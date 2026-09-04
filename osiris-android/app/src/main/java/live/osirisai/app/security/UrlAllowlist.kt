package live.osirisai.app.security

import java.net.URI
import java.util.Locale

/**
 * Strict host allowlist for WebView navigation and resource loads.
 * Uses java.net.URI so unit tests run on the JVM without Robolectric.
 * Primary origin is osirisai.live; additional HTTPS hosts are required for tiles/CDN assets.
 */
object UrlAllowlist {

    const val PRIMARY_HOST = "osirisai.live"

    /** Hosts allowed for top-level navigation (in-app WebView). */
    private val navigationHosts: Set<String> = setOf(
        PRIMARY_HOST,
        "www.osirisai.live",
    )

    /**
     * Hosts allowed for subresource loads (scripts, tiles, fonts, images).
     * Keep conservative — unknown hosts open in Custom Tabs instead of the WebView.
     */
    private val resourceHosts: Set<String> = navigationHosts + setOf(
        "basemaps.cartocdn.com",
        "a.basemaps.cartocdn.com",
        "b.basemaps.cartocdn.com",
        "c.basemaps.cartocdn.com",
        "d.basemaps.cartocdn.com",
        "tiles.stadiamaps.com",
        "api.maptiler.com",
        "tile.openstreetmap.org",
        "a.tile.openstreetmap.org",
        "b.tile.openstreetmap.org",
        "c.tile.openstreetmap.org",
        "fonts.googleapis.com",
        "fonts.gstatic.com",
        "cdn.jsdelivr.net",
        "unpkg.com",
    )

    fun normalizeHost(host: String?): String? =
        host?.lowercase(Locale.US)?.trim()?.trimEnd('.')?.takeIf { it.isNotEmpty() }

    fun parseUri(url: String?): URI? {
        if (url.isNullOrBlank()) return null
        return runCatching { URI(url.trim()) }.getOrNull()
    }

    fun isHttps(uri: URI): Boolean =
        uri.scheme.equals("https", ignoreCase = true)

    fun isBlockedScheme(uri: URI): Boolean {
        val scheme = uri.scheme?.lowercase(Locale.US) ?: return true
        return scheme in BLOCKED_SCHEMES
    }

    /** Android WebView Uri adapter without depending on android.net in pure logic tests. */
    fun isBlockedScheme(scheme: String?): Boolean {
        val s = scheme?.lowercase(Locale.US) ?: return true
        return s in BLOCKED_SCHEMES
    }

    fun isNavigationAllowed(url: String?): Boolean {
        val uri = parseUri(url) ?: return false
        if (isBlockedScheme(uri)) return false
        if (!isHttps(uri)) return false
        val host = normalizeHost(uri.host) ?: return false
        return host in navigationHosts || host.endsWith(".$PRIMARY_HOST")
    }

    fun isResourceAllowed(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        when {
            url.startsWith("data:", ignoreCase = true) -> return true
            url.startsWith("blob:", ignoreCase = true) -> return true
            url.startsWith("about:blank", ignoreCase = true) -> return true
        }
        val uri = parseUri(url) ?: return false
        if (isBlockedScheme(uri)) return false
        if (!isHttps(uri)) return false
        val host = normalizeHost(uri.host) ?: return false
        if (host in resourceHosts) return true
        if (host.endsWith(".$PRIMARY_HOST")) return true
        if (host.endsWith(".cartocdn.com")) return true
        if (host.endsWith(".tile.openstreetmap.org")) return true
        return false
    }

    fun isPrimaryOrigin(url: String?): Boolean = isNavigationAllowed(url)

    fun sanitizeSharePayload(raw: String?, maxLen: Int = 2_000): String? {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim().take(maxLen)
        if (trimmed.any { ch -> ch.isISOControl() && ch != '\n' && ch != '\t' }) {
            return null
        }
        return trimmed
    }

    fun isSafeShareUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val uri = parseUri(url) ?: return false
        if (!isHttps(uri)) return false
        val host = normalizeHost(uri.host) ?: return false
        return host == PRIMARY_HOST || host.endsWith(".$PRIMARY_HOST")
    }

    private val BLOCKED_SCHEMES = setOf(
        "file",
        "content",
        "javascript",
        "intent",
        "intent-filter",
        "android-app",
        "market",
        "tel",
        "sms",
        "mailto",
        "ftp",
        "ws",
        "wss",
    )
}

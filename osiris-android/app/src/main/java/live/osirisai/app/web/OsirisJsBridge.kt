package live.osirisai.app.web

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import live.osirisai.app.R
import live.osirisai.app.security.UrlAllowlist

/**
 * Narrow, validated bridge exposed to the OSIRIS page as `OsirisNative`.
 * Never evaluate untrusted strings; all inputs are sanitized.
 */
class OsirisJsBridge(
    private val context: Context,
    private val callbacks: Callbacks,
) {
    interface Callbacks {
        fun onShare(text: String)
        fun onRequestLocation()
        fun onOpenAbout()
        fun onHaptic()
        fun onLog(message: String)
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun share(payload: String?) {
        val sanitized = UrlAllowlist.sanitizeSharePayload(payload) ?: return
        // Prefer URL share when payload is a safe OSIRIS URL; otherwise share as text
        mainHandler.post {
            callbacks.onHaptic()
            callbacks.onShare(sanitized)
        }
    }

    @JavascriptInterface
    fun shareUrl(url: String?) {
        if (!UrlAllowlist.isSafeShareUrl(url)) return
        mainHandler.post {
            callbacks.onHaptic()
            callbacks.onShare(url!!.trim())
        }
    }

    @JavascriptInterface
    fun requestLocation() {
        mainHandler.post {
            callbacks.onHaptic()
            callbacks.onRequestLocation()
        }
    }

    @JavascriptInterface
    fun openAbout() {
        mainHandler.post {
            callbacks.onOpenAbout()
        }
    }

    @JavascriptInterface
    fun ping(): String = "osiris-native-1"

    companion object {
        const val NAME = "OsirisNative"

        fun launchShareChooser(context: Context, payload: String) {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, payload)
            }
            val chooser = Intent.createChooser(
                send,
                context.getString(R.string.share_chooser),
            )
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }
}

package live.osirisai.app.web

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import live.osirisai.app.R
import live.osirisai.app.security.UrlAllowlist

object CustomTabsHelper {

    fun open(context: Context, url: String) {
        if (!UrlAllowlist.isNavigationAllowed(url) && !url.startsWith("https://", ignoreCase = true)) return
        val uri = Uri.parse(url)
        if (uri.scheme?.equals("https", ignoreCase = true) != true) return
        if (uri.host.isNullOrBlank()) return

        val color = ContextCompat.getColor(context, R.color.osiris_black)
        val params = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(color)
            .setNavigationBarColor(color)
            .build()

        val intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(params)
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .build()

        runCatching {
            intent.launchUrl(context, uri)
        }.onFailure {
            val fallback = Intent(Intent.ACTION_VIEW, uri)
            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallback)
        }
    }
}

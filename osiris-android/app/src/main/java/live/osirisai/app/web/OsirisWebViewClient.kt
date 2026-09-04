package live.osirisai.app.web

import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import live.osirisai.app.security.UrlAllowlist

sealed interface WebLoadEvent {
    data object PageStarted : WebLoadEvent
    data object PageFinished : WebLoadEvent
    data class HttpError(val code: Int, val description: String) : WebLoadEvent
    data class SslBlocked(val url: String?) : WebLoadEvent
    data class ExternalUrl(val url: String) : WebLoadEvent
    data class NavigationBlocked(val url: String?) : WebLoadEvent
}

class OsirisWebViewClient(
    private val onEvent: (WebLoadEvent) -> Unit,
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url?.toString()
        if (url.isNullOrBlank()) return true

        if (UrlAllowlist.isBlockedScheme(request.url)) {
            onEvent(WebLoadEvent.NavigationBlocked(url))
            return true
        }

        if (UrlAllowlist.isNavigationAllowed(url)) {
            return false
        }

        // External HTTPS → Custom Tabs via shell
        if (UrlAllowlist.isHttps(request.url)) {
            onEvent(WebLoadEvent.ExternalUrl(url))
            return true
        }

        onEvent(WebLoadEvent.NavigationBlocked(url))
        return true
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        val url = request.url?.toString() ?: return deny()
        if (!UrlAllowlist.isResourceAllowed(url)) {
            return deny()
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        onEvent(WebLoadEvent.PageStarted)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        onEvent(WebLoadEvent.PageFinished)
        injectBridgeBootstrap(view)
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError,
    ) {
        if (request.isForMainFrame) {
            onEvent(
                WebLoadEvent.HttpError(
                    code = error.errorCode,
                    description = error.description?.toString() ?: "Load error",
                ),
            )
        }
    }

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse,
    ) {
        if (request.isForMainFrame && errorResponse.statusCode >= 400) {
            onEvent(
                WebLoadEvent.HttpError(
                    code = errorResponse.statusCode,
                    description = errorResponse.reasonPhrase ?: "HTTP error",
                ),
            )
        }
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        // Never proceed on SSL errors — fail closed
        handler?.cancel()
        onEvent(WebLoadEvent.SslBlocked(error?.url))
    }

    private fun deny(): WebResourceResponse =
        WebResourceResponse(
            "text/plain",
            "utf-8",
            java.io.ByteArrayInputStream(ByteArray(0)),
        )

    private fun injectBridgeBootstrap(view: WebView?) {
        if (view == null) return
        // Expose a small helper the page (or console) can use; does not eval user input
        val script = """
            (function(){
              if (window.__osirisNativeReady) return;
              window.__osirisNativeReady = true;
              window.OsirisShell = {
                available: !!(window.OsirisNative && window.OsirisNative.ping),
                share: function(t){ try { window.OsirisNative.share(String(t||'')); } catch(e){} },
                shareUrl: function(u){ try { window.OsirisNative.shareUrl(String(u||'')); } catch(e){} },
                locate: function(){ try { window.OsirisNative.requestLocation(); } catch(e){} },
                about: function(){ try { window.OsirisNative.openAbout(); } catch(e){} }
              };
              document.dispatchEvent(new CustomEvent('osiris-native-ready'));
            })();
        """.trimIndent()
        view.evaluateJavascript(script, null)
    }
}

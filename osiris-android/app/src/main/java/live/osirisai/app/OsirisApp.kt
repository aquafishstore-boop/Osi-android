package live.osirisai.app

import android.app.Application
import android.webkit.WebView

class OsirisApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }
}

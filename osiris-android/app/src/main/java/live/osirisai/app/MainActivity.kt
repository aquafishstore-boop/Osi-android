package live.osirisai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import live.osirisai.app.security.UrlAllowlist
import live.osirisai.app.ui.shell.OsirisShellScreen
import live.osirisai.app.ui.theme.OsirisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val deepLink = intent?.data?.toString()
        val initialUrl = if (UrlAllowlist.isNavigationAllowed(deepLink)) {
            deepLink!!
        } else {
            BuildConfig.OSIRIS_ORIGIN
        }

        setContent {
            OsirisTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    OsirisShellScreen(initialUrl = initialUrl)
                }
            }
        }
    }
}

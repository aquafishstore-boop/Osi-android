package live.osirisai.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import live.osirisai.app.security.UrlAllowlist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmokeInstrumentedTest {

    @Test
    fun packageName_isOsiris() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertTrue(appContext.packageName.startsWith("live.osirisai.app"))
    }

    @Test
    fun buildConfig_pointsAtLiveOrigin() {
        assertEquals("https://osirisai.live", BuildConfig.OSIRIS_ORIGIN)
        assertTrue(UrlAllowlist.isNavigationAllowed(BuildConfig.OSIRIS_ORIGIN))
    }
}

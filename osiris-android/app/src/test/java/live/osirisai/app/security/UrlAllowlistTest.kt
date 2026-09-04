package live.osirisai.app.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class UrlAllowlistTest {

    @Test
    fun navigation_allowsPrimaryOrigin() {
        assertTrue(UrlAllowlist.isNavigationAllowed("https://osirisai.live/"))
        assertTrue(UrlAllowlist.isNavigationAllowed("https://osirisai.live/docs"))
        assertTrue(UrlAllowlist.isNavigationAllowed("https://www.osirisai.live/"))
    }

    @Test
    fun navigation_blocksHttpAndForeignHosts() {
        assertFalse(UrlAllowlist.isNavigationAllowed("http://osirisai.live/"))
        assertFalse(UrlAllowlist.isNavigationAllowed("https://evil.example/"))
        assertFalse(UrlAllowlist.isNavigationAllowed("https://osirisai.live.evil.com/"))
    }

    @Test
    fun navigation_blocksDangerousSchemes() {
        assertFalse(UrlAllowlist.isNavigationAllowed("file:///sdcard/x"))
        assertFalse(UrlAllowlist.isNavigationAllowed("javascript:alert(1)"))
        assertFalse(UrlAllowlist.isNavigationAllowed("intent://scan/#Intent;end"))
        assertFalse(UrlAllowlist.isNavigationAllowed("content://media/1"))
    }

    @Test
    fun resources_allowTilesAndData() {
        assertTrue(UrlAllowlist.isResourceAllowed("https://basemaps.cartocdn.com/dark_all/1/1/1.png"))
        assertTrue(UrlAllowlist.isResourceAllowed("https://a.basemaps.cartocdn.com/x"))
        assertTrue(UrlAllowlist.isResourceAllowed("data:image/png;base64,aaa"))
        assertTrue(UrlAllowlist.isResourceAllowed("blob:https://osirisai.live/uuid"))
        assertFalse(UrlAllowlist.isResourceAllowed("https://tracker.ads.example/pixel"))
    }

    @Test
    fun sharePayload_sanitizes() {
        assertNull(UrlAllowlist.sanitizeSharePayload(null))
        assertNull(UrlAllowlist.sanitizeSharePayload("\u0000bad"))
        assertEquals("hello", UrlAllowlist.sanitizeSharePayload("  hello  "))
        val long = "x".repeat(5_000)
        assertEquals(2_000, UrlAllowlist.sanitizeSharePayload(long)!!.length)
    }

    @Test
    fun shareUrl_onlyOsirisHttps() {
        assertTrue(UrlAllowlist.isSafeShareUrl("https://osirisai.live/?lat=1&lon=2"))
        assertFalse(UrlAllowlist.isSafeShareUrl("https://evil.com/phish"))
        assertFalse(UrlAllowlist.isSafeShareUrl("http://osirisai.live/"))
        assertFalse(UrlAllowlist.isSafeShareUrl("javascript:alert(1)"))
    }
}

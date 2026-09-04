package live.osirisai.app.web

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.webkit.WebView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationBridge {

    fun hasPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(context: Context): Location? {
        if (!hasPermission(context)) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()
        return suspendCancellableCoroutine { cont ->
            cont.invokeOnCancellation { cts.cancel() }
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    if (cont.isActive) cont.resume(location)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        }
    }

    fun injectLocation(webView: WebView, latitude: Double, longitude: Double) {
        // Clamp to valid ranges; never inject raw strings from untrusted sources
        val lat = latitude.coerceIn(-90.0, 90.0)
        val lon = longitude.coerceIn(-180.0, 180.0)
        val script = """
            (function(){
              var detail = { lat: $lat, lon: $lon, source: 'osiris-native' };
              window.__osirisLastLocation = detail;
              document.dispatchEvent(new CustomEvent('osiris-native-location', { detail: detail }));
              if (typeof window.map !== 'undefined' && window.map && window.map.flyTo) {
                try { window.map.flyTo({ center: [$lon, $lat], zoom: Math.max(window.map.getZoom()||3, 8) }); } catch(e){}
              }
            })();
        """.trimIndent()
        webView.evaluateJavascript(script, null)
    }

    fun injectEscapeOrClosePanel(webView: WebView, onResult: (Boolean) -> Unit) {
        // Try Escape to close panels; report whether history can go back afterward
        val script = """
            (function(){
              try {
                document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', code: 'Escape', keyCode: 27, bubbles: true }));
                return true;
              } catch(e) { return false; }
            })();
        """.trimIndent()
        webView.evaluateJavascript(script) { onResult(true) }
    }
}

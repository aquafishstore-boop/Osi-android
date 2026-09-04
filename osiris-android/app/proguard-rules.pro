# Keep JavascriptInterface methods for the OSIRIS bridge
-keepclassmembers class live.osirisai.app.web.OsirisJsBridge {
    @android.webkit.JavascriptInterface <methods>;
}

-keepattributes JavascriptInterface
-keepattributes *Annotation*

# WebView / AndroidX WebKit
-dontwarn android.webkit.**

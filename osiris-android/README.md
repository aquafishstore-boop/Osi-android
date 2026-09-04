# OSIRIS Android Shell

A polished, security-hardened Android client for the [OSIRIS](https://osirisai.live) global intelligence platform.

This is provide free, as is, its purpose is to use the excellent osirisai.live , Osiris platofrm in a mobile andriod wrapper, with some ux/ui optimisation tools for mobile and tablets.

The app embeds `https://osirisai.live` in a lockdown WebView and adds native splash, offline/error handling, back navigation, share & optional location bridges, and an About sheet. Map controls (layers, intel, RECON, search) come from OSIRIS’s existing mobile UI.

## Requirements

- Android Studio Ladybug+ (or AGP 8.7-compatible)
- JDK 17
- Android device or emulator with **OpenGL ES 3.0** (physical device recommended for MapLibre WebGL)
- `minSdk 26` / `targetSdk 35`

## Open & run

1. Open the `osiris-android` folder in Android Studio.
2. Let Gradle sync (wrapper uses Gradle 8.9).
3. Run the **app** configuration on a device/emulator.

From the CLI (with JDK 17 and Android SDK configured):

```bash
cd osiris-android
./gradlew :app:assembleDebug
./gradlew :app:test
```

On Windows:

```powershell
cd osiris-android
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:test
```

Create `local.properties` if Android Studio does not (point `sdk.dir` at your SDK).

## Security model

| Control | Behavior |
|--------|----------|
| HTTPS only | Cleartext disabled via network security config |
| Navigation allowlist | Only `osirisai.live` (and www) inside the WebView |
| Resource allowlist | Primary origin + known map CDN hosts; `data:` / `blob:` allowed for WebGL |
| External links | Open in Chrome Custom Tabs |
| SSL errors | Cancelled (fail closed) — never `handler.proceed()` |
| WebView lockdown | No file/content access, no mixed content, Safe Browsing on |
| JS bridge | Narrow `OsirisNative` API; sanitized share payloads |
| Permissions | Internet always; location optional at runtime |
| Backup | WebView data excluded from backup |

Certificate pinning is intentionally **not** enabled in v1 (would break on normal cert rotation for a third-party origin).

## Native bridge

The page can call (also available as `window.OsirisShell`):

- `OsirisNative.share(text)` / `shareUrl(url)` — Android Sharesheet (URL must be `https://osirisai.live…`)
- `OsirisNative.requestLocation()` — optional locate → injects `osiris-native-location` event
- `OsirisNative.openAbout()` — About sheet
- `OsirisNative.ping()` — returns `osiris-native-1`

## Package & release

- Application ID: `live.osirisai.app`
- Copy `keystore.properties.example` → `keystore.properties` and wire signing in `app/build.gradle.kts` before Play upload.

## Attribution

Upstream OSIRIS is open source (MIT) by SimplifAI Soul: https://github.com/simplifaisoul/osiris  

This wrapper is an independent mobile shell; it does not fork the Next.js app.

## Responsible use

Only point RECON / active scan tools at infrastructure you own or have written authorization to test.

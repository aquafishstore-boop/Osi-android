# Local Docker build + Playwright mobile E2E

## Build the APK (Docker)

No host JDK/Android SDK required.

```bash
cd osiris-android
docker compose -f docker-compose.build.yml build
docker compose -f docker-compose.build.yml run --rm android-build
```

APK lands at `artifacts/app-debug.apk`.

Install on a physical device / Android Studio emulator:

```bash
adb install -r artifacts/app-debug.apk
```

> Running a full Android emulator *inside* Docker Desktop on Windows is unreliable
> without nested KVM. Prefer host emulator or a phone for APK smoke tests.

## Playwright (mobile WebView proxy)

These tests hit `https://osirisai.live` with Pixel 7 / iPhone 14 viewports — the same UI the hardened WebView loads.

```bash
cd osiris-android/e2e
npm install
npx playwright install chromium
npm test
```

Results: `e2e/results/` (JSON, HTML report, screenshots, metrics).

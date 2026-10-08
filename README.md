# chand

An Android home-screen widget app for Persian date and USD/Toman display.

## Current hardened build

- Android application ID: `com.chand.mobiletina`
- Source namespace: `com.chand.mobiletina`
- Main and test source trees are fully migrated to `com/chand/mobiletina`.
- Version: `1.6.6` (`versionCode 27`)
- R8 minification/resource shrinking enabled for hardened/release builds.
- Native JNI bridge uses dynamic registration and hidden symbols.
- Hardened builds verify the current APK signing certificate at process startup and widget/worker
  entry points, and apply native anti-debug/hook checks. A modified APK re-signed with another
  certificate fails closed on launch; this does not prevent a determined attacker from patching
  client-side checks or rebuilding public source.
- Combined widget uses host-aware sizing and `fitCenter` rendering for Samsung One UI while retaining the MIUI resize path.
- A small theme control in the app repaints all widgets in light, dark or device-based mode. Light mode retains the original white widget appearance.
- Instagram and Telegram actions use drawn icons; empty dollar values and change indicators have no dash placeholder.

## USD price updates

- The USD free-market quote is fetched from TGJU's public dollar profile without an API key.
- If that page fails or lacks a valid quote, the public TGJU currency table is checked.
- Rates are read only from the USD current-price field and converted from rial to toman once.
- WorkManager requests an update every 15 minutes and when the app opens or a dollar widget is tapped; Android controls the actual execution time.
- The app screen observes saved quotes and refreshes without reopening the app. Failed requests retain the last successful quote and its existing timestamp.
- Public-page access depends on TGJU's availability and markup; future site changes may require a parser update.

## CI

GitHub Actions runs unit tests, lint and `assembleHardened` and publishes the hardened APK artifact.

## Release signing

The installable CI hardened artifact uses a local/generated signing identity whose certificate digest is compiled into that artifact. For long-term public updates, replace it with a persistent private release key stored outside the repository (for example GitHub Secrets or Play App Signing) and retain that key permanently.

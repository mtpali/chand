# chand

An Android home-screen widget app for Persian date and USD/Toman display.

## Current hardened build

- Android application ID: `com.chand.mobiletina`
- Source namespace: `com.chand.mobiletina`
- Main and test source trees are fully migrated to `com/chand/mobiletina`.
- Version: `1.6.4` (`versionCode 25`)
- R8 minification/resource shrinking enabled for hardened/release builds.
- Native JNI bridge uses dynamic registration and hidden symbols.
- Hardened builds verify the current APK signing certificate at process startup and widget/worker
  entry points, and apply native anti-debug/hook checks. A modified APK re-signed with another
  certificate fails closed on launch; this does not prevent a determined attacker from patching
  client-side checks or rebuilding public source.
- Combined widget uses host-aware sizing and `fitCenter` rendering for Samsung One UI while retaining the MIUI resize path.

## CI

GitHub Actions runs unit tests, lint and `assembleHardened` and publishes the hardened APK artifact.

## Release signing

The installable CI hardened artifact uses a local/generated signing identity whose certificate digest is compiled into that artifact. For long-term public updates, replace it with a persistent private release key stored outside the repository (for example GitHub Secrets or Play App Signing) and retain that key permanently.

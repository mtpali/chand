# chand

An Android home-screen widget app for Persian date and USD/Toman display.

## Current hardened build

- Android application ID: `com.chand.mobiletina`
- Source namespace remains `com.mtpali.chand` intentionally to preserve stable internal JNI/component wiring while R8 repackages non-entrypoint implementation classes.
- Version: `1.6.2` (`versionCode 23`)
- R8 minification/resource shrinking enabled for hardened/release builds.
- Native JNI bridge uses dynamic registration and hidden symbols.
- Hardened builds enforce runtime signing-certificate integrity and native anti-debug/hook checks.
- Combined widget uses host-aware sizing and `fitCenter` rendering for Samsung One UI while retaining the MIUI resize path.

## CI

GitHub Actions runs unit tests, lint and `assembleHardened` and publishes the hardened APK artifact.

## Release signing

The installable CI hardened artifact uses a local/generated signing identity whose certificate digest is compiled into that artifact. For long-term public updates, replace it with a persistent private release key stored outside the repository (for example GitHub Secrets or Play App Signing) and retain that key permanently.

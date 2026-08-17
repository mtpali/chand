# chand

Android app focused on iOS-like home-screen widgets for Persian date and USD price.

## Widgets
- Persian date widget
- USD widget
- Combined iOS-style date + USD widget

The widgets are rendered to Bitmap and displayed through a simple RemoteViews ImageView for better compatibility with MIUI, Samsung One UI and other vendor launchers.

## Hardened build
The `hardened` build type enables R8 full mode, resource shrinking, non-debuggable Java/native code, certificate locking, and distributed runtime integrity checks. The runtime guard validates package identity, debuggable state and the installed signing certificate. The native vault independently checks process identity, debugger attachment and common hook/instrumentation traces.

The hardened signing identity is generated locally and stored only under the ignored `.gradle/chand-secure/` directory. The matching SHA-256 signing certificate is compiled into that APK, so a binary edit followed by re-signing with another key invalidates the runtime certificate lock. For long-term public distribution, use a persistent private release key stored outside the repository or Play App Signing.

No client-side Android protection can make an APK mathematically impossible to modify. These controls are intended to make simple APK patching, MT Manager edit/re-sign workflows and common runtime instrumentation substantially harder.

## Build
CI uses JDK 17, Android SDK 36, NDK 27.2.12479018, CMake 3.22.1 and Gradle 9.1.0.

The CI workflow runs unit tests, Android Lint and `assembleHardened`, then uploads `chand-hardened-apk`.

## License
See `LICENSE`. Redistribution, rebranding and derivative distribution are restricted by the project license.

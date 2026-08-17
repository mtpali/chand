# chand

Android app focused on iOS-like home-screen widgets for Persian date and USD price.

## Widgets
- Persian date widget
- USD widget
- Combined iOS-style date + USD widget

The widgets are rendered to Bitmap and displayed through a simple RemoteViews ImageView for better compatibility with MIUI and other vendor launchers.

## Current hardened build
The `hardened` build type enables R8/resource shrinking, non-debuggable native code, certificate locking, and distributed runtime integrity checks. The runtime guard validates the installed signing certificate, package identity and debugger state, while the native vault also checks process identity and common hook/instrumentation traces.

The hardened CI signing identity is generated locally and stored only under the ignored `.gradle/chand-secure/` directory. Re-signing a modified APK with another certificate invalidates the runtime certificate lock. For long-term public distribution, use a persistent private release key stored outside the repository or use Play App Signing.

No client-side Android protection can make an APK mathematically impossible to modify; these controls are intended to substantially raise the cost of binary patching and simple edit/re-sign workflows.

## Build
CI uses JDK 17, Android SDK 36, NDK 27.2.12479018, CMake 3.22.1 and Gradle 9.1.0.

The CI workflow runs unit tests, Android Lint and `assembleHardened`, then uploads `chand-hardened-apk`.

## License
See `LICENSE`. Redistribution, rebranding and derivative distribution are restricted by the project license.

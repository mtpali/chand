# chand

An Android home-screen widget app for Persian date and USD/Toman display.

## Current hardened build

- Android application ID: `com.chand.mobiletina`
- Source namespace: `com.chand.mobiletina`
- Main and test source trees are fully migrated to `com/chand/mobiletina`.
- Version: `1.7.2` (`versionCode 30`)
- R8 minification/resource shrinking enabled for hardened/release builds.
- Native JNI bridge uses dynamic registration and hidden symbols.
- Hardened builds independently verify the current APK signing certificate in Kotlin and native
  code at process startup and widget/worker entry points, and apply native anti-debug/hook checks.
  The native pin is generated from the build's actual signer. A modified APK re-signed with another
  certificate fails closed on launch; this does not prevent a determined attacker from patching
  client-side checks or rebuilding public source.
- Combined widget uses host-aware sizing and `fitCenter` rendering for Samsung One UI while retaining the MIUI resize path.
- A small theme control in the app repaints all widgets in light, dark or device-based mode. Light mode retains the original white widget appearance.
- Instagram and Telegram actions use drawn icons; empty dollar values and change indicators have no dash placeholder.

## USD price updates

- The first source is the public JSON endpoint `https://hansha.online/l2`, matching the supplied reference app.
- Only the exact `usd` row's most recent timestamped sale price (`sp`) is accepted. Hansha prices are already in toman.
- Invalid, missing, stale (over 72 hours) or failed Hansha quotes fall back to AlanChand's homepage USD sale cell, then its dedicated USD page.
- Only if AlanChand also fails, use TGJU's dollar profile, then TGJU's currency table: **Hansha → AlanChand → TGJU**.
- AlanChand's visible homepage price is in toman; its dedicated Product/Offer JSON-LD explicitly declares IRR (converted once) or IRT (kept as toman).
- TGJU rates are read only from the identified USD current-price field and converted from rial to toman once. Tether and other currencies are never substituted for USD.
- WorkManager requests an update every 15 minutes and when the app opens or a dollar widget is tapped; Android controls the actual execution time.
- The app screen observes saved quotes and refreshes without reopening the app. Failed requests retain the last successful quote and its existing timestamp.
- Each successful quote is compared with the previous successful quote. An identical price shows **بدون تغییر** in the app, dollar widget and combined widget; rises and falls retain their arrows and amounts.
- Network requests use bounded timeouts and response sizes. If all sources fail, the last successful quote and movement remain visible.
- Public-source availability and schema/markup changes can require parser updates.
- Source addresses are decoded at runtime and the catalog is protected against optimizer folding. This raises the cost of APK string editing; public endpoints are not authentication secrets.

## Typography and launcher icon

- The app, platform theme and all three bitmap widgets share the bundled Vazirmatn 33.003 family, including the Latin USD label.
- Regular, Medium, SemiBold and Bold match the font files in the user-provided reference APK; file hashes are recorded in `docs/FONTS.json`.
- Fonts are bundled in the repository and installed APK. Build-time font downloading has been removed.
- The OFL license is included in `licenses/Vazirmatn-OFL.txt`.
- The launcher uses the reference coin vector with a white/grey background, with adaptive round and Android 13 monochrome variants.
- The Persian calendar, widget sizing, white default appearance, theme picker and 15-minute schedule are retained.

## CI

GitHub Actions runs unit tests, lint and `assembleHardened` and publishes the hardened APK artifact.
It also launches the original APK on an emulator and requires startup rejection after controlled
re-signing, including a case where the Kotlin certificate pin is patched to trust the test signer.

## Release signing

The installable CI hardened artifact uses a local/generated signing identity whose certificate digest is compiled into that artifact. For long-term public updates, replace it with a persistent private release key stored outside the repository (for example GitHub Secrets or Play App Signing) and retain that key permanently.

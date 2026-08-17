import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.security.KeyStore
import java.security.MessageDigest

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Fonts are pinned to a specific upstream release and downloaded only at build time.
// The installed APK is fully self-contained for typography.
val widgetFontDir = layout.projectDirectory.dir("src/main/res/font")
val downloadWidgetFonts by tasks.registering {
    val regular = widgetFontDir.file("vazirmatn_regular.ttf")
    val bold = widgetFontDir.file("vazirmatn_bold.ttf")
    outputs.files(regular, bold)

    doLast {
        widgetFontDir.asFile.mkdirs()
        val files = mapOf(
            regular.asFile to "https://raw.githubusercontent.com/rastikerdar/vazirmatn/v33.003/fonts/ttf/Vazirmatn-Regular.ttf",
            bold.asFile to "https://raw.githubusercontent.com/rastikerdar/vazirmatn/v33.003/fonts/ttf/Vazirmatn-Bold.ttf"
        )
        files.forEach { (target, source) ->
            if (!target.exists() || target.length() < 100_000L) {
                URI(source).toURL().openStream().use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
            }
        }
    }
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(downloadWidgetFonts)
}

fun certificateSha256(store: File, password: String, alias: String): String {
    val keyStore = KeyStore.getInstance("PKCS12")
    FileInputStream(store).use { input ->
        keyStore.load(input, password.toCharArray())
    }
    val certificate = keyStore.getCertificate(alias)
        ?: error("Signing certificate was not found for alias $alias")
    return MessageDigest.getInstance("SHA-256")
        .digest(certificate.encoded)
        .joinToString(separator = "") { byte -> "%02X".format(byte) }
}

// CI/test hardened APKs get a fresh signing identity during configuration. The matching
// certificate digest is compiled into that exact APK. Re-signing a modified APK therefore
// invalidates the runtime certificate lock. For a public release, replace this generated key
// with a persistent private release key from GitHub Secrets / Play App Signing.
val hardenedAlias = "chand_hardened"
val hardenedPassword = "chand-ci-hardened-963"
val hardenedStore = rootProject.layout.projectDirectory
    .file(".gradle/chand-secure/chand-hardened.p12")
    .asFile
if (!hardenedStore.exists()) {
    hardenedStore.parentFile.mkdirs()
    val keytool = File(System.getProperty("java.home"), "bin/keytool").absolutePath
    val process = ProcessBuilder(
        keytool,
        "-genkeypair",
        "-noprompt",
        "-alias", hardenedAlias,
        "-keyalg", "RSA",
        "-keysize", "3072",
        "-sigalg", "SHA256withRSA",
        "-validity", "3650",
        "-dname", "CN=chand hardened,O=mtpali,C=IR",
        "-storetype", "PKCS12",
        "-keystore", hardenedStore.absolutePath,
        "-storepass", hardenedPassword,
        "-keypass", hardenedPassword
    )
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().readText()
    check(process.waitFor() == 0) { "Unable to create hardened signing key: $output" }
}
val hardenedCertSha256 = certificateSha256(hardenedStore, hardenedPassword, hardenedAlias)

android {
    // Keep the source namespace stable so JNI/Manifest component names remain compatible.
    // The installed Android package/application ID is intentionally branded separately.
    namespace = "com.mtpali.chand"
    compileSdk = 36
    ndkVersion = "27.2.12479018"

    defaultConfig {
        applicationId = "com.chand.mobiletina"
        minSdk = 26
        targetSdk = 36
        versionCode = 23
        versionName = "1.6.2"

        buildConfigField("boolean", "SECURE_RUNTIME", "false")
        buildConfigField("String", "CERT_LOCK_SHA256", "\"\"")

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    signingConfigs {
        create("hardenedCi") {
            storeFile = hardenedStore
            storePassword = hardenedPassword
            keyAlias = hardenedAlias
            keyPassword = hardenedPassword
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isJniDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        // Installable anti-tamper CI artifact. It is certificate-locked to the key generated
        // above, so a binary edit + re-sign cycle no longer produces a normally runnable app.
        create("hardened") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("hardenedCi")
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isJniDebuggable = false
            buildConfigField("boolean", "SECURE_RUNTIME", "true")
            buildConfigField("String", "CERT_LOCK_SHA256", "\"$hardenedCertSha256\"")
            matchingFallbacks += listOf("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        jniLibs.useLegacyPackaging = false
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.06.00")
    implementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.work:work-runtime:2.11.2")

    testImplementation("junit:junit:4.13.2")
}

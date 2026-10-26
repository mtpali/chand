import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.MessageDigest

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

// The same OFL-licensed, pinned fonts are bundled for Compose and every widget.
// Builds and installed typography no longer depend on a font download task.

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

// Hardened APKs use a dedicated signing identity and compile that certificate digest into
// the binary. Re-signing a modified APK invalidates the runtime certificate lock.
// For public distribution, replace this local/generated identity with a persistent private
// release key kept outside the repository or use Play App Signing.
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
        "-dname", "CN=chand hardened,O=mobiletina,C=IR",
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

// A second signer pin is compiled into native code, independently of the DEX constant.
val nativePinDirectory = layout.buildDirectory.dir("generated/integrity").get().asFile
val maskedPin = hardenedCertSha256.chunked(2).mapIndexed { index, byte ->
    byte.toInt(16) xor ((0xA7 + index * 13) and 0xff)
}
val nativePinHeader = """
    #pragma once
    #include <cstdint>
    static constexpr std::uint8_t kMaskedSignerPin[] = {${maskedPin.joinToString(",")}};
""".trimIndent() + "\n"
nativePinDirectory.mkdirs()
val nativePinFile = File(nativePinDirectory, "runtime_pin.h")
if (!nativePinFile.exists() || nativePinFile.readText() != nativePinHeader) {
    nativePinFile.writeText(nativePinHeader)
}

android {
    namespace = "com.chand.mobiletina"
    compileSdk = 36
    ndkVersion = "27.2.12479018"

    defaultConfig {
        applicationId = "com.chand.mobiletina"
        minSdk = 26
        targetSdk = 36
        versionCode = 30
        versionName = "1.7.2"

        buildConfigField("boolean", "SECURE_RUNTIME", "false")
        buildConfigField("String", "CERT_LOCK_SHA256", "\"\"")

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }

        externalNativeBuild {
            cmake {
                arguments += "-DCHAND_PIN_INCLUDE_DIR=${nativePinDirectory.absolutePath}"
            }
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
    // Android supplies org.json at runtime; JVM parser tests need its real implementation.
    testImplementation("org.json:json:20250517")
}

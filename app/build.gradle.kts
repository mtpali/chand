import java.net.URI

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val alanChandToken = providers.gradleProperty("ALANCHAND_TOKEN").orNull ?: ""

// Fonts are pinned to a specific upstream release and downloaded at build time.
// The installed APK contains the fonts and does not need internet for typography.
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

android {
    namespace = "com.mtpali.chand"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mtpali.chand"
        minSdk = 26
        targetSdk = 36
        versionCode = 15
        versionName = "1.5.2"

        buildConfigField("String", "ALANCHAND_TOKEN", "\"$alanChandToken\"")
        buildConfigField("String", "ALANCHAND_API_URL", "\"https://api.alanchand.com\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
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
    testImplementation("org.json:json:20250517")
}

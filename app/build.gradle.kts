import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.github.triplet.play")
}

val keystorePropertiesFile = rootProject.file("keystore/keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}

// One-time manual step only Mutaz can do: Play Console -> Setup -> API access,
// create/link a service account with Release manager access on this app, then
// drop its JSON key at play/service-account.json (gitignored, same pattern as
// keystore/). Without that file, `./gradlew publishReleaseBundle` cannot auth.
val playServiceAccountFile = rootProject.file("play/service-account.json")

android {
    namespace = "com.mutazyounes.prayerathan"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mutazyounes.prayerathan"
        minSdk = 26
        targetSdk = 36
        versionCode = 26
        versionName = "0.26.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

play {
    // Internal track: 4701680380313434468 (see store/README.md). Bundle only,
    // this package rejects APK uploads. Draft by default so a push here still
    // needs a manual "Publish" click in Console unless releaseStatus is changed.
    if (playServiceAccountFile.exists()) {
        serviceAccountCredentials.set(playServiceAccountFile)
    }
    track.set("internal")
    defaultToAppBundles.set(true)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("com.batoulapps.adhan:adhan2:0.0.7")
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}

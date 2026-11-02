plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

/*
 * Signing release.
 * Nilai TIDAK disimpan di source. Dibaca dari environment variable (dipakai GitHub Actions)
 * atau dari gradle property (~/.gradle/gradle.properties) untuk build lokal:
 *   DROIDXAMPP_KEYSTORE_FILE / droidxampp.keystoreFile
 *   DROIDXAMPP_KEYSTORE_PASSWORD / droidxampp.keystorePassword
 *   DROIDXAMPP_KEY_ALIAS / droidxampp.keyAlias
 *   DROIDXAMPP_KEY_PASSWORD / droidxampp.keyPassword
 * Bila salah satu tidak ada, APK release dihasilkan TANPA tanda tangan (app-release-unsigned.apk).
 * Debug signing tidak pernah dipakai untuk build release.
 */
fun signingValue(env: String, prop: String): String? =
    System.getenv(env)?.takeIf { it.isNotBlank() }
        ?: (findProperty(prop) as String?)?.takeIf { it.isNotBlank() }

val ksFile = signingValue("DROIDXAMPP_KEYSTORE_FILE", "droidxampp.keystoreFile")
val ksPassword = signingValue("DROIDXAMPP_KEYSTORE_PASSWORD", "droidxampp.keystorePassword")
val ksAlias = signingValue("DROIDXAMPP_KEY_ALIAS", "droidxampp.keyAlias")
val ksKeyPassword = signingValue("DROIDXAMPP_KEY_PASSWORD", "droidxampp.keyPassword")
val hasReleaseSigning = ksFile != null && ksPassword != null && ksAlias != null && ksKeyPassword != null &&
    file(ksFile).exists()

if (!hasReleaseSigning) {
    logger.warn("DroidXAMPP: keystore release tidak lengkap/tidak ditemukan. APK release akan TIDAK ditandatangani.")
}

android {
    namespace = "com.droidxampp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.droidxampp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(ksFile!!)
                storePassword = ksPassword
                keyAlias = ksAlias
                keyPassword = ksKeyPassword
            }
        }
    }

    buildTypes {
        release {
            // R8 sengaja dimatikan pada Tahap 1 agar build stabil; dievaluasi setelah uji perangkat.
            isMinifyEnabled = false
            signingConfig = if (hasReleaseSigning) signingConfigs.getByName("release") else null
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = false
        htmlReport = true
        xmlReport = true
        lintConfig = file("lint.xml")
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation("junit:junit:4.13.2")
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt") // ✅ Necesario para usar Room
}

android {
    namespace = "com.cibertec.pe.netshop"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cibertec.pe.netshop"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13" // Compatibilidad con Compose 2024.09.00
    }
}

dependencies {
    // Compose BOM
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.firebase.crashlytics.buildtools)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")


    // Jetpack Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("io.coil-kt:coil-compose:2.4.0")
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Navegación Compose
    implementation(libs.androidx.navigation.compose)

    // Material Icons
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    // ✅ Room (compatible con Kotlin 2.0.21)
    implementation("androidx.room:room-runtime:2.7.0-alpha02")
    implementation("androidx.room:room-ktx:2.7.0-alpha02")
    kapt("androidx.room:room-compiler:2.7.0-alpha02")

    // Otros
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

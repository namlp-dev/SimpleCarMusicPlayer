plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.automusic.automotivemusicplayer"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.automusic.automotivemusicplayer"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Bật ViewBinding cho UI
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)

    // Lifecycle & Activity
    implementation(libs.bundles.lifecycle)
    implementation(libs.activity.ktx)

    // Media3
    implementation(libs.bundles.media3)

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Glide
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // Voice & Car App
//    implementation(libs.mlkit.speech)
    implementation(libs.car.app)
    implementation(libs.car.app.automotive) // Thư viện bắt buộc cho Automotive OS

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
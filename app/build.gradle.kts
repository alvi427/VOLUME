plugins {
    id("com.android.application")
}

android {
    namespace = "com.alvi.volumeslider"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.alvi.volumeslider"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

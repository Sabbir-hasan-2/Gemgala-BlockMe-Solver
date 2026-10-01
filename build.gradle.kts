plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.codex.blockmesolver"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.codex.blockmesolver"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}

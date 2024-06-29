// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply {
        set("room_version", "2.6.0")
        set("nav_version", "2.5.3")
        set("lifecycle_version", "2.6.2")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}


//android {
//    compileSdk = 33
//
//    defaultConfig {
//        applicationId = "com.example.yourapp"
//        minSdk = 21
//        targetSdk = 33
//        versionCode = 1
//        versionName = "1.0"
//    }
//
//    buildTypes {
//        getByName("debug") {
//            isDebuggable = true
//        }
//        getByName("release") {
//            isMinifyEnabled = false
//            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//        }
//    }
//}
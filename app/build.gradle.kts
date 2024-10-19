plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file("/home/fahim/AndroidStudioProjects/SMSReverse/key.jks")
            storePassword = "64742812"
            keyAlias = "key0"
            keyPassword = "64742812"
        }
        create("release") {
            storeFile = file("/home/fahim/AndroidStudioProjects/SMSReverse/key.jks")
            storePassword = "64742812"
            keyAlias = "key0"
            keyPassword = "64742812"
        }
    }
    namespace = "com.horoftech.smsreverse"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.horoftech.smsreverse"
        minSdk = 24
        targetSdk = 34
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
    buildFeatures{
        viewBinding = true
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation (libs.browser)
    implementation (libs.okhttp)
    implementation (libs.logging.interceptor)
    implementation (libs.okio)


    implementation (libs.play.services.auth)
    implementation(libs.firebase.database)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
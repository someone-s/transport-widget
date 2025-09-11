plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.jaredsburrows.license)
}

// https://developer.android.com/training/data-storage/room/migrating-db-versions#set_schema_location_using_room_gradle_plugin
room {
    schemaDirectory("$projectDir/schemas")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

android {

    namespace = "com.eden.livewidget"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.eden.livewidget"
        minSdk = 24
        targetSdk = 36
        versionCode = 10000
        versionName = "1.0.0"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true

    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.window)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.squareup.retrofit2.retrofit)
    implementation(libs.squareup.retrofit2.converter.gson)
    implementation(libs.me.xdrop.fuzzywuzzy)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.sqlite.bundled)
    androidTestImplementation(libs.androidx.room.testing)
    implementation(libs.requery.sqlite.android)
    ksp(libs.androidx.room.compiler)
    annotationProcessor(libs.androidx.room.compiler)
}

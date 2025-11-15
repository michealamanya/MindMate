plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "micheal.must.signuplogin"
    compileSdk = 36

    defaultConfig {
        applicationId = "micheal.must.signuplogin"
        minSdk = 24
        targetSdk = 36
        versionCode = 6
        versionName = "1.10"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Use project root relative path
            storeFile = file("${rootProject.projectDir}/release.keystore")

            // Load from gradle.properties or environment variables
            storePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: project.findProperty("KEYSTORE_PASSWORD")?.toString()
            keyAlias = System.getenv("KEY_ALIAS")
                ?: project.findProperty("KEY_ALIAS")?.toString()
                        ?: "release_key"
            keyPassword = System.getenv("KEY_PASSWORD")
                ?: project.findProperty("KEY_PASSWORD")?.toString()
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
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

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
        // Fix for 16 KB page size compatibility
        jniLibs {
            useLegacyPackaging = false
        }
    }

    androidResources {
        noCompress += listOf("tflite", "lite")
    }
}

dependencies {
    // ============================================
    // AndroidX & UI Libraries
    // ============================================
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation(libs.swiperefreshlayout)
    implementation("androidx.biometric:biometric:1.1.0")

    // ============================================
    // Firebase (BOM manages all Firebase versions)
    // ============================================
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-config")

    // ============================================
    // Google Sign-In & Credentials
    // ============================================
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation("com.google.android.gms:play-services-auth:21.4.0")

    // ============================================
    // Google AI
    // ============================================
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // ============================================
    // Image Loading - Glide
    // ============================================
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ============================================
    // TensorFlow Lite (Machine Learning)
    // ============================================
    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")
    implementation("com.google.mlkit:face-detection:16.1.7")

    // ============================================
    // Utility Libraries
    // ============================================
    implementation("com.google.guava:guava:31.0.1-android")
    implementation("org.reactivestreams:reactive-streams:1.0.4")
    implementation("org.jsoup:jsoup:1.16.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // ============================================
    // Testing
    // ============================================
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
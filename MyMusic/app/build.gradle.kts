plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.mymusic"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mymusic"
        minSdk = 24
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")
    implementation ("com.google.firebase:firebase-firestore:24.11.1")

    implementation("com.google.android.material:material:1.11.0")

    implementation ("com.airbnb.android:lottie:6.1.0")

    implementation("io.github.jan-tennert.supabase:storage-kt:3.2.3")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.2.3")        // nhớ đổi gotrue-kt → auth-kt, vì tên module trước đây đã thay đổi
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.2.3")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services") version "4.4.2"
    id ("org.sonarqube") version "4.3.0.3225"
}

sonarqube {
    properties {
        property ("sonar.projectKey", "Webziee_GGAPP")
        property ("sonar.organization", "webziee") // for SonarCloud
        property ("sonar.host.url", "https://sonarcloud.io/summary/overall?id=Webziee_GGAPP") // or your SonarQube server URL
        property ("sonar.login", "TOKEN") // Optional for SonarCloud
    }
}


android {
    namespace = "com.guesthouse.ggapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.guesthouse.ggapp"
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        // Exclude duplicate META-INF files
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE-FIREBASE.txt"
            excludes += "META-INF/NOTICE"
        }
    }

}

dependencies {

    // Retrofit for API requests
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp for HTTP logging (optional but helpful for debugging)
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")

    // Google Maps
    implementation ("com.google.android.gms:play-services-maps:18.0.2")

    // Material UI
    implementation ("com.google.android.material:material:1.9.0")

    // Other dependencies
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    implementation ("com.google.firebase:firebase-config:21.3.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore.ktx)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Glide for image loading
    implementation ("com.github.bumptech.glide:glide:4.15.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.0")
    implementation ("jp.wasabeef:glide-transformations:4.3.0")

    // Import the Firebase BoM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))

    //push noyifications firebase
    implementation ("com.google.firebase:firebase-messaging")

    /*
    The following code is used for biometric authentication, this code was inspired from the following video:
    Lackner, P., 2024. Youtube, How to Implement Biometric Auth in Your Android App. [Online]
    Available at: https://www.youtube.com/watch?v=_dCRQ9wta-I
    [Accessed 12 October 2024].*/
    implementation("androidx.biometric:biometric:1.1.0")

    // Firebase Authentication (keep only one `firebase-auth` dependency) (TechWorld, 2023)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-auth:21.0.1")
    implementation("com.google.firebase:firebase-storage:20.0.0")

    implementation ("com.squareup.okhttp3:okhttp:4.9.1")

}

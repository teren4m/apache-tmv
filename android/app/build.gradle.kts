plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.github.teren4m.tvm.test"

    defaultConfig {
        applicationId = "com.github.teren4m.tvm.test"
        minSdk = 30
        targetSdk = 34
        compileSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        dataBinding = false
        viewBinding = true
    }
    ndkVersion = "25.1.8937393"
    externalNativeBuild {
        cmake {
            version = "3.22.1"
        }
    }

}

dependencies {
    implementation(files("libs/tvm4j-core-0.0.1-SNAPSHOT.jar"))
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.markodevcic:peko:3.0.4")
    implementation("com.google.guava:guava:32.1.3-android")
}

kapt {
    correctErrorTypes = true
}

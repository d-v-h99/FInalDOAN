plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.chaquo.python")
    id("androidx.navigation.safeargs")
    id ("kotlin-kapt")
    id ("kotlin-parcelize")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.hoangdoviet.finaldoan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hoangdoviet.finaldoan"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf(  "armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
        version = "3.8"
    }
    chaquopy {
        defaultConfig {
            version = "3.8"
        }
        defaultConfig {
            pip {
                install("newspaper3k")
            }
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
    configurations {
        implementation {
            exclude(module = "guava-jdk5")
        }
        all {
            exclude(group = "com.google.guava", module = "listenablefuture")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.androidx.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.ai.client.generativeai:generativeai:0.2.2")
    implementation ("org.jsoup:jsoup:1.14.3")
    implementation ("com.github.zagum:SpeechRecognitionView:1.2.2")

    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.fragment:fragment-ktx:1.7.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    //
    implementation ("androidx.navigation:navigation-fragment:2.7.7")
    implementation ("androidx.navigation:navigation-ui:2.7.7")
    implementation ("com.contrarywind:Android-PickerView:4.1.9")
    //
    implementation ("com.github.prolificinteractive:material-calendarview:1.6.0")
    implementation ("pub.devrel:easypermissions:3.0.0")
    //animation
    implementation ("com.airbnb.android:lottie:6.4.0")
    //
    implementation ("it.xabaras.android:recyclerview-swipedecorator:1.4")
    //
    implementation ("com.google.android.gms:play-services-auth:15.0.0")
    implementation("com.google.api-client:google-api-client-android:1.23.0") {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation("com.google.apis:google-api-services-calendar:v3-rev310-1.23.0") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "com.google.guava")
    }
    implementation ("pub.devrel:easypermissions:1.2.0")
    implementation ("com.github.andrefrsousa:SuperBottomSheet:2.0.0")
}

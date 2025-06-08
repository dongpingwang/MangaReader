plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.jetbrains.kotlin.kapt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.wolf2.reader"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.wolf2.reader"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "v0.2.0-20250601"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }

        ndk {
            abiFilters += "arm64-v8a"
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }

    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    // https://github.com/Reginer/aosp-android-jar

    // https://github.com/JakeWharton/timber
    implementation(libs.timber)

    // https://github.com/Tencent/MMKV
    // https://github.com/liangjingkanji/Serialize
    // https://github.com/DylanCaiCoding/MMKV-KTX
    implementation(libs.mmkv.ktx)

    // https://github.com/nanihadesuka/LazyColumnScrollbar
    implementation(libs.lazycolumnscrollbar)

    // https://github.com/oleksandrbalan/pagecurl
    implementation(project(":pagecurl"))

    // https://github.com/smartword-app/compose-swipeable-cards
    // https://github.com/Aghajari/LazySwipeCards
    implementation(project(":swipeable_cards"))

    // https://github.com/oothp/PdfiumAndroid
    // implementation(project(":PdfiumAndroid"))

    // https://github.com/measure-sh/measure
    // implementation(project(":measure"))

    // https://github.com/jhy/jsoup
    implementation(libs.jsoup)

    // https://github.com/saket/telephoto
    // https://github.com/usuiat/Zoomable
    implementation (libs.zoomable)

    // https://github.com/saket/swipe
    // implementation(libs.swipe)

    // https://github.com/saket/cascade
    implementation(libs.cascade)
    implementation(libs.cascade.compose)

    // https://github.com/valentinilk/compose-shimmer
    // implementation("com.valentinilk.shimmer:compose-shimmer:1.3.3")

    //implementation ("me.saket.bytesize:bytesize:2.0.0-beta04")


    implementation ("androidx.paging:paging-runtime:3.2.1")
    implementation ("androidx.paging:paging-compose:3.3.0") // Compose 扩展
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.room:room-paging:2.7.1")

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.compose.animation.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.google.android.material)
    implementation(libs.coil.kt.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.room.runtime)
    kapt(libs.room.compiler)
    // ksp(libs.room.compiler)
    annotationProcessor(libs.room.compiler)
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)

    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test)
    debugImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

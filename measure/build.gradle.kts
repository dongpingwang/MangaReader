@file:Suppress("UnstableApiUsage")

import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
    alias(libs.plugins.diffplug.spotless)
    alias(libs.plugins.compose.compiler)
}

private val measureSdkVersion = "v1.0"

android {
    namespace = "sh.measure.android"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        defaultConfig {
            buildConfigField("String", "MEASURE_SDK_VERSION", "\"$measureSdkVersion\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "consumer-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    externalNativeBuild {
        cmake {
            path("CMakeLists.txt")
        }
    }
    packaging {
        jniLibs.pickFirsts += "**/libmeasure-ndk.so"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

extensions.configure<SpotlessExtension>("spotless") {
    plugins.withId("org.jetbrains.kotlin.jvm") {
        configureSpotlessKotlin(this@configure)
    }
    plugins.withId("org.jetbrains.kotlin.android") {
        configureSpotlessKotlin(this@configure)
    }
    kotlinGradle {
        ktlint()
    }
    format("misc") {
        target(
            ".gitignore",
            ".gitattributes",
            ".gitconfig",
            ".editorconfig",
            "*.md",
            "src/**/*.md",
            "docs/**/*.md",
            "src/**/*.properties",
        )
        indentWithSpaces()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

fun configureSpotlessKotlin(spotlessExtension: SpotlessExtension) {
    spotlessExtension.kotlin {
        ktlint().apply {
            editorConfigOverride(
                mapOf(
                    "max_line_length" to 2147483647,
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                ),
            )
        }
        target("src/**/*.kt")
    }
}

dependencies {
    // Compile only, as we don't want to include the fragment dependency in the final artifact.
    compileOnly(libs.androidx.fragment.ktx)
    compileOnly(libs.androidx.compose.runtime.android)
    compileOnly(libs.androidx.compose.ui)
    compileOnly(libs.androidx.navigation.compose)
    compileOnly(libs.androidx.navigation.fragment)
    compileOnly(libs.squareup.okhttp)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.squareup.okio)
    implementation(libs.squareup.curtains)
}

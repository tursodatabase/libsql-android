plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.rust.android)
}

apply(plugin = "org.mozilla.rust-android-gradle.rust-android")

android {
    namespace = "tech.turso.libsql"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")

        // follow AGP 8.4 default: https://developer.android.com/build/releases/gradle-plugin#compatibility
        ndkVersion = "26.1.10909125"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // follow AGP 8.4 default: https://developer.android.com/build/releases/gradle-plugin#compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

cargo {
    module = "./src/main/rust/"
    libname = "libsql_android"
    targets = listOf("arm", "arm64", "x86", "x86_64")
    exec = { spec, _ ->
        spec.environment("ANDROID_NDK_HOME", android.ndkDirectory.path)

        // default clang version for NDK 26.1.10909125, NDK 27^ uses 18
        spec.environment("NDK_CLANG_VERSION", 17)
    }
}

// taken from https://github.com/firezone/firezone/blob/e856dc5eb2052eff48212b94a6ebf86b2c1e67df/kotlin/android/app/build.gradle.kts#L247-L250
tasks.matching { it.name.matches(Regex("merge.*JniLibFolders")) }.configureEach {
    inputs.dir(layout.buildDirectory.file("rustJniLibs/android"))
    dependsOn("cargoBuild")
}

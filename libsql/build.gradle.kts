import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.proto

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.rust.android)

    id("com.google.protobuf") version "0.9.4"
    id("com.diffplug.spotless") version "6.25.0"
}

apply(plugin = "org.mozilla.rust-android-gradle.rust-android")

android {
    namespace = "tech.turso.libsql"
    compileSdk = 34

    sourceSets["main"].proto {
        srcDir("src/main/proto")
    }

    sourceSets["main"].java {
        srcDir("build/generated/source/proto")
    }

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")

        // follow AGP 8.4 default: https://developer.android.com/build/releases/gradle-plugin#compatibility
        ndkVersion = "26.1.10909125"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    // follow AGP 8.4 default: https://developer.android.com/build/releases/gradle-plugin#compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation(libs.ext.junit)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.rules)
    // implementation(libs.protobuf.javalite)
    implementation("com.google.protobuf:protobuf-java:3.24.0")
}

cargo {
    module = "./src/main/rust/"
    libname = "libsql_android"
    targets = listOf("arm")
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

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.0.0"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("java") {
                    java { }
                }
            }
        }
    }
}

spotless {
    java {
        target("src/*/java/**/*.java")
        importOrder()
        cleanthat()
        googleJavaFormat().aosp()
        formatAnnotations()
    }
    kotlinGradle {
        target("*.gradle.kts") // default target for kotlinGradle
        ktlint() // or ktfmt() or prettier()
    }
}

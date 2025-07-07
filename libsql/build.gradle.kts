import cn.lalaki.pub.BaseCentralPortalPlusExtension
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.proto

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.rust.android)
    alias(libs.plugins.jetbrains.kotlin.android)

    id("com.google.protobuf") version "0.9.4"
    id("com.diffplug.spotless") version "6.25.0"

    id("cn.lalaki.central") version "1.2.8"
    id("maven-publish")
    signing
}

apply(plugin = "org.mozilla.rust-android-gradle.rust-android")

android {
    namespace = "tech.turso.libsql"
    compileSdk = 34

    sourceSets {
        named("main") {
            proto {
                srcDir("src/main/proto")
            }
        }

        named("debug") {
            java {
                srcDir("build/generated/source/proto/debug")
            }
        }

        named("release") {
            java {
                srcDir("build/generated/source/proto/release")
            }
        }
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

    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.protobuf.java)
    implementation(libs.core.ktx)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.rules)
}

cargo {
    if (gradle.startParameter.taskNames.any { it.lowercase().contains("release") }) {
        profile = "release"
    } else {
        profile = "debug"
    }
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

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.17.3"
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
    kotlin {
        target("src/*/kotlin/**/*.kt")
        ktlint()
    }
    kotlinGradle {
        target("*.gradle.kts") // default target for kotlinGradle
        ktlint()
    }
}

var local = uri(layout.buildDirectory.dir("staging-deploy"))
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "tech.turso.libsql"
            artifactId = "libsql"
            version = "0.1.1"

            afterEvaluate {
                from(components.getByName("release"))
            }

            pom {
                name = artifactId
                description = "Java bindings for libSQL"

                url = "https://github.com/tursodatabase/libsql-android"

                licenses {
                    license {
                        name = "The MIT License"
                        url = "https://opensource.org/license/MIT"
                    }
                }

                developers {
                    developer {
                        id = "haaawk"
                        name = "Piotr Jastrzebski"
                        email = "piotr@turso.tech"
                    }

                    developer {
                        id = "levydsa"
                        name = "Levy Albuquerque"
                        email = "levy@turso.tech"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/tursodatabase/libsql_android.git"
                    developerConnection = "scm:git:ssh://github.com:tursodatabase/libsql_android.git"
                    url = "https://github.com/tursodatabase/libsql_android"
                }
            }
        }
    }

    repositories {
        maven {
            name = "stagingDeploy"
            url = local
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications.getByName("release"))
}

tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(tasks.withType<Sign>())
}

val sonatypeUsername: String by project
val sonatypePassword: String by project

centralPortalPlus {
    url = local
    username = sonatypeUsername
    password = sonatypePassword
    publishingType = BaseCentralPortalPlusExtension.PublishingType.AUTOMATIC
}
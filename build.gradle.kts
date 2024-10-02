group = "tech.turso"
version = "0.1.0"

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.rust.android) apply false
    id("org.jetbrains.kotlin.android") version "2.0.10" apply false
    id("com.google.protobuf") version "0.9.4" apply false
}

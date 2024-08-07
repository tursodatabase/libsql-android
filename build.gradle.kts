group = "tech.turso"
version = "0.1"

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.rust.android) apply false
    id("com.google.protobuf") version "0.9.4" apply false
    // alias(libs.plugins.protobuf) apply false
    // alias(libs.plugins.protobuf)
}

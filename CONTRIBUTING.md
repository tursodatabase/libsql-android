# Contributor Manual

Turso welcomes contributions from the community. This manual provides guidelines for contributing to `libsql-android` SDK.

Make sure to [join us on Discord](https://tur.so/discord-php) &mdash; (`#libsql-android` channel) to discuss your ideas and get help.

## Prerequisites

- Android Studio (latest version recommended)
- Java Development Kit (JDK) 17 or higher
- Android SDK (API level 21 or higher)
- Rust and Cargo (latest stable version)
- Android NDK (version 26.1.10909125 or compatible)

## Making Changes

1. Create a new branch for your feature or bug fix: `git switch -c my-new-feature`
2. Make changes and commit them with a clear commit message
3. Push your changes to your fork: `git push origin my-new-feature`
4. Open a Pull Request on the main repository

## Development Setup

1. Fork and clone the repository
2. Open the project in Android Studio
3. Make sure you have the Rust plugin installed in Android Studio
4. Set up the `ANDROID_NDK_HOME` environment variable to point to your NDK installation
5. Sync the project with Gradle files

## Running the Project Locally

1. Open the project in Android Studio
2. Wait for the Gradle sync to complete
3. Select a target device (emulator or physical device)
4. Click `Run`

## Building the Library

To build the library, run the following command from the project root:

```bash
./gradlew :libsql:assembleRelease
```

This will generate the AAR file in the `libsql/build/outputs/aar/` directory.

5. Create a new branch for your feature or bug fix: `git switch -c my-new-feature`
6. Make changes and commit them with a clear commit message
7. Push your changes to your fork `git push origin my-new-feature`
8. Open a Pull Request on the main repository

## Running Tests

To run the instrumented tests, use the following command:

```bash
./gradlew :libsql:connectedAndroidTest
```

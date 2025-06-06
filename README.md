# GrooveBox

A modern, cross-platform music player built with Kotlin Multiplatform and Jetpack Compose.

## Features

- 🎵 Music playback with play/pause, skip forward/backward controls
- 🔊 Volume control
- 📱 Cross-platform support for Android, iOS, and Desktop
- 🎨 Modern Material 3 design
- 🌐 Client-server architecture with Ktor

## Technologies

GrooveBox is built using modern technologies:

- **Kotlin Multiplatform** (v2.1.21) - Share code across platforms
- **Jetpack Compose Multiplatform** (v1.9.0-alpha02) - UI framework
- **Material 3** - Design system
- **Koin** (v4.1.0) - Dependency injection
- **Ktor** (v3.1.3) - Networking and server components
- **BasicPlayer** (v3.0.0.0) - Audio playback
- **Kotlinx Coroutines** (v1.10.2) - Asynchronous programming

## Project Structure

- `/composeApp` - Shared Compose Multiplatform code
    - `commonMain` - Code shared across all platforms
    - Platform-specific folders for Android, iOS, and Desktop
- `/iosApp` - iOS application entry point
- `/server` - Ktor server application
- `/shared` - Code shared between all targets

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) or [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- [Kotlin Multiplatform Mobile plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile)
- [Java JDK 11](https://adoptium.net/) or newer
- [Xcode](https://developer.apple.com/xcode/) (for iOS development)

### Setup

1. Clone the repository:
   ```
   git clone https://github.com/renaudmathieu/GrooveBox.git
   cd GrooveBox
   ```

2. Open the project in Android Studio or IntelliJ IDEA.

3. Sync the project with Gradle files.

### Running the Application

#### Android

- Select the Android configuration and run on an emulator or physical device.

#### iOS

- Open the `iosApp` project in Xcode and run on a simulator or physical device.
- Alternatively, use the Kotlin Multiplatform Mobile plugin to run on iOS.

#### Desktop

- Run the desktop configuration to launch the application on your computer.

#### Server

- Run the server module to start the Ktor server.

## Development

### Hot Reload

The project supports Compose Hot Reload for faster development:

```
./gradlew :composeApp:developmentExecutableAndroidDevelopmentDebug
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Koin](https://insert-koin.io/)
- [Ktor](https://ktor.io/)
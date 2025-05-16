# Real-Time Chat App

A single-screen mobile chat application built with Jetpack Compose, Clean Architecture, and SOLID principles. Supports real-time communication via Socket.IO and offline message queuing.

## Features
- Real-time chat with socket-based updates (PieHost)
- Offline message queue and auto-retry
- Jetpack Compose UI
- Clean Architecture (data, domain, presentation)
- SOLID principles

## Architecture
```
app/
  └── src/main/java/com/dev/yatin/chatapp/
      ├── data/        # Room, Socket, RepositoryImpl
      ├── domain/      # Models, Repository interfaces, UseCases
      ├── di/          # Hilt DI
      └── presentation/# ViewModel, Compose UI
```

## Setup
1. Clone the repo
2. Open in Android Studio
3. Build the project (Gradle sync)
4. Run on an emulator or device

## Dependencies
- Jetpack Compose
- Hilt
- Room
- Socket.IO client
- Kotlin Coroutines

## How it works
- **Chat list**: Shows all conversations with latest message preview
- **Real-time**: Messages sync instantly via sockets
- **Offline**: Failed messages are queued and retried automatically
- **App close**: Conversations are cleared (in-memory only)

## Customization
- Update the SocketService URL in `AppModule.kt` for your own backend

---

**Author:** [Your Name] 
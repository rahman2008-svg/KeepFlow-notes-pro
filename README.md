# KeepFlow Notes 📝

KeepFlow Notes is an elegant, production-ready, fully offline-first **Google Keep clone** built using Kotlin, Jetpack Compose, and modern Android development best practices. It features intuitive Material 3 design, local data persistence, search and tagging organization, and background reminders using JobScheduler / WorkManager integrations.

---

## ✨ Features

- **Notes & Checklists**: Easily write rich textual notes or dynamic checklist todo lists with progress tracking.
- **Pinning & Archival**: Keep essential resources pinned at the top of your feeds, and archive tasks out of sight cleanly.
- **Dynamic Categorization (Labels)**: Create, manage, and label notes dynamically to easily filter views.
- **Advanced State Flow Reminders**: Attach date and time alerts to notes. These utilize high-precision local notifications and handle overdue events robustly via system alarms.
- **Premium Material 3 Canvas**: Implements modern dynamic Light/Dark elements, grid & list toggle transitions, elegant margins, and high-quality iconography.
- **Offline First**: Fully self-contained local caching engine running completely private without external API trackers or backends.

---

## 🛠️ Tech Stack & Architecture

This application strictly targets the **Clean Architecture pattern (MVVM)** to promote highly modular, independent, and testable systems.

- **Presentation Layer (`UI & Composable Elements`)**:
  - **Jetpack Compose (Material 3)**: Adaptive layouts (flows, masonry-style grids, responsive list cards).
  - **StateFlow & CollectAsStateWithLifecycle**: Unidirectional UI state rendering.
  - **Jetpack Navigation Component**: Type-safe navigation controller routing.
  - **MVVM Architecture**: Driven by `NoteViewModel` lifecycle scope handling.
- **Domain Layer (`UseCases & Data Models`)**:
  - Independent entities (`Note`, `Label`, `ChecklistItem`).
  - Strict mapping for business operations (`GetAllNotesUseCase`, `SaveNoteUseCase`, `DeleteNote`, etc.).
- **Data Layer (`Persistence & API Implementation`)**:
  - **Room Database**: Multitrim local SQLite indexing, fast asynchronous IO operations, relational mappings, and converters.
- **System Utilities & Services**:
  - **WorkManager**: Executes robust, persistent reminder scheduling that survives app termination or system reboots.
  - **NotificationManager**: Schedules local channels for high-importance visual reminder overlays on Android.

---

## 🏗️ Folder Structure

```
/app/src/main/java/com/example
│
├── data
│   ├── local                  # Room Database, DAOs, Converters, Entities
│   └── repository             # Room Data Layer repositories (implementations)
│
├── domain
│   ├── model                  # Note, Label, and Checklist entities
│   ├── repository             # Repository interfaces (Contracts)
│   └── usecase                # Clean business services (GetAllNotes, Search, delete)
│
├── presentation
│   ├── components             # NoteCard, ColorPicker, and custom widgets
│   ├── screens                # HomeScreen, NoteEditScreen layout engines
│   └── viewmodel            # MVVM viewmodels and state managers
│
└── utils                      # NotificationHelper, ReminderWorker lifecycle services
```

---

## 🚀 CI/CD Support Included

This project contains pre-configured automation pathways:
- **GitHub Actions (`/.github/workflows/android.yml`)**: Compiles, runs lints, assembles production/debug APKs, and archives build artifacts on every push or PR.
- **Codemagic Integration (`/codemagic.yaml`)**: Built for clean workflows syncing mac-runners with standard Zulu JDK 17 gradle tasks.

---

## 💻 Setup & Build Instructions

1. **Clone & Open**: Open this repository directory directly in the latest edition of Android Studio.
2. **Build Configurations**: Sync the Gradle wrapper configuration to download Jetpack Compose and Room SDKs automatically.
3. **Run Application**: Press `Shift + F10` or the Run button in Android Studio to push the application to your emulator or hardware terminal!

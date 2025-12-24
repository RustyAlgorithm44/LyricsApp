# LyricsApp - Carnatic Music Edition

A specialized, fully **offline Android app** designed for Carnatic music enthusiasts to store, manage, and view song lyrics. The app is built with a modern Android architecture and Material Design principles, providing a clean and intuitive user experience.

This project is a continuously evolving Android application, demonstrating best practices in Kotlin, MVVM, Room, and Material You theming.

---

## ✨ Features

### 🎶 Carnatic Music Library
*   **Add & Edit Songs:** Store songs with Title, Composer, Deity, Lyrics, and an optional YouTube link.
*   **Input Validation:** Prevents adding songs with blank titles or lyrics.
*   **Rich Song Display:** View songs in a clean, filterable list using `MaterialCardView` for a modern look.

###  Organization & Navigation
*   **Home Page Filtering:** Organize and filter songs by "Deity" or "Composer" using `ChipGroup`.
*   **Grouped Views:** Drill down from a list of all deities or composers to a filtered song list.
*   **Navigation Drawer:** Easy access to Home, Favorites, and Settings.
*   **Favorites:** Mark songs as favorites and view them in a dedicated "Favorites" screen.

### 🔍 Smart Search & Actions
*   **Robust Search:** Instantly search across song titles, composers, deities, and lyrics.
*   **Contextual Actions:** Long-press on any song to enter a contextual action mode to edit or delete.
*   **Visual Selection Feedback:** Selected items are highlighted, and a second tap deselects.

### 📖 Reading & Sharing
*   **Pinch-to-Zoom:** Zoom and pan lyrics in the detail view for better readability.
*   **Share Functionality:** Share song details with others.
*   **Multi-Language Support:** Store lyrics for a single song in multiple languages (e.g., English, Tamil, Sanskrit) and easily switch between them in the detail view.

### 🎨 Modern UI & Theming
*   **Material 3 Design:** Incorporates modern Material Design components.
*   **Dynamic Color:** Supports Material You dynamic theming on Android 12+.
*   **Theme Switcher:** Manually switch between Light, Dark, and System default themes in Settings.
*   **Modernized Dialogs:** Clean, scrollable dialogs for adding and editing songs.

### 💾 Offline Backup & Restore
*   **Local Backup:** Export your entire song library to a JSON file.
*   **Non-Destructive Import:** Restore from a backup without creating duplicates, now including the `youtubeLink` field. The import logic is additive and avoids overwriting existing data.

---

## 🛠️ Tech Stack

*   **Language:** Kotlin
*   **UI:** XML with Material Design 3 Components
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Database:** Room Persistence Library (SQLite) with migrations
*   **Asynchronous Programming:** Kotlin Coroutines & Flow
*   **View Components:** RecyclerView, `MaterialCardView`, `ChipGroup`, `TextInputLayout`
*   **Dependency Injection:** Manual (via `ViewModelFactory`)
*   **Minimum SDK:** 24 (Android 7.0+)

---

## 🏗️ App Architecture

The app follows a standard MVVM pattern to ensure a scalable and maintainable codebase.

*   **Model:** `Song.kt` (Room Entity), `AppDatabase.kt` (database holder), `SongDao.kt` (data access objects), and `SongRepository.kt` (single source of truth).
*   **View:** XML layouts and Activities (`MainActivity`, `SongDetailActivity`, `SettingsActivity`, `FavoritesActivity`) that observe data from the ViewModel.
*   **ViewModel:** `SongViewModel.kt` prepares and manages data for the UI, interacting with the `SongRepository`.

---

## 🚀 Getting Started (For Developers)

1.  Clone this repository.
2.  Open the project in **Android Studio**.
3.  Let Gradle sync the dependencies.
4.  Run on an emulator or a physical device.

---

## 🔒 Privacy

*   **100% Offline:** The app does not require an internet connection.
*   **No Ads or Analytics:** Your data is yours alone.
*   **Local Storage:** All data is stored exclusively on your device.
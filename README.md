# 🎵 LyricsApp (Offline Lyrics Manager)

A simple, fully **offline Android app** to store, read, search, and share song lyrics.
Built as a lightweight personal lyrics vault — no internet, no ads, no tracking.

This is my **first Android app**, built from scratch using Kotlin and Android Studio.

---

## ✨ Features

### 📚 Lyrics Library

* Add songs with:

  * Song
  * Composer
  * Category
  * Full lyrics
* View all songs in a clean, scrollable list
* Tap a song to open a dedicated **lyrics detail screen**

### 🔍 Smart Search

* Search instantly across:

  * Title
  * Artist
  * Category
  * Lyrics content
* Works fully offline using Room database queries

### 📤 Share Lyrics

* Share formatted lyrics via WhatsApp, email, notes, etc.
* Format:

  ```
  Song Title – Artist

  [Full lyrics]
  ```

### 💾 Offline Backup & Restore

* Export all lyrics as a **JSON backup**
* Choose where to save the file (shareable & user-visible)
* Import backup anytime to restore data
* No internet required

### 🗑️ Manage Data

* View total number of songs in Settings
* Delete all songs (with confirmation)
* Data updates instantly

### 🧭 Navigation

* Bottom navigation:

  * Songs
  * Settings
* Gesture navigation supported

### 📖 Reading Experience

* Scrollable lyrics view

---

## 🛠️ Tech Stack

* **Language:** Kotlin
* **UI:** XML layouts (Material Design)
* **Architecture:** MVVM
* **Database:** Room (SQLite)
* **Async:** Kotlin Coroutines
* **Lists:** RecyclerView
* **Storage:** Android Storage Access Framework
* **Minimum SDK:** 24 (Android 7.0+)
* **Offline-first:** ✅ 100%

---

## 🏗️ App Architecture

```
com.guruguhan.lyricsapp
│
├── data
│   ├── Song.kt
│   ├── SongDao.kt
│   └── SongRepository.kt
│
├── backup
│   └── BackupManager.kt
│
├── ui
│   └── SongAdapter.kt
│
├── MainActivity.kt
├── SongDetailActivity.kt
└── SettingsActivity.kt
```

---

## 🚀 Getting Started (For Developers)

1. Clone the repo
2. Open in **Android Studio**
3. Let Gradle sync
4. Run on emulator or physical device
5. Build → Generate Signed APK (for sideloading)

---

## 📦 Release

* Signed release APK available under **GitHub Releases**
* Designed for **personal use / sideloading**
* No Play Store dependency

---

## 🔒 Privacy

* No internet access
* No ads
* No analytics
* All data stays on the device

---

## 📌 Future Improvements (Ideas)

* Favorites ⭐
* Sort by title / artist
* Duplicate detection on import
* Dark mode fine-tuning
* Fragment-based navigation
* Adding reference youtube video link
* New song adding menu shouldn't accept blank values

---

## 🙌 Acknowledgements

Built while learning Android development step by step, with a focus on:

* Clean architecture
* Practical UX
* Offline reliability

---

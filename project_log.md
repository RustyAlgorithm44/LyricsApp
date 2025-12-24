## LyricsApp Project Log

**Overview:**
The LyricsApp is an Android application designed to help users store, manage, and view song lyrics. It utilizes a standard MVVM (Model-View-ViewModel) architecture with Room for local data persistence. The app is customized for Carnatic music.

**Current Features:**
*   **Add Songs:** Users can add new songs with their titles, composer, deity, lyrics, and optional YouTube link.
*   **Search Songs:** Users can search for songs by title, composer, deity, or lyrics.
*   **Edit/Delete Songs:** Users can edit or delete existing songs from the collection via long-press on the song list.
*   **View Song List:** Displays a list of all stored songs.
*   **Contextual Action Mode:** Manual implementation for edit/delete in the main toolbar.
*   **Side Panel (Navigation Drawer):** Access to Settings and Share (placeholder).
*   **Pinch-to-Zoom Lyrics:** Improved functionality in `SongDetailActivity` allowing scaling and panning.
*   **Home Page Organization:** Filter songs by Deity or Composer using Chips.

**Architecture Summary:**
The application follows a consistent MVVM architectural pattern using Kotlin, Room Persistence Library, ViewModel, and Flow.
*   **Model:** `Song.kt` defines the data structure for a song. `AppDatabase.kt` and `SongDao.kt` handle the database interactions. `SongRepository.kt` acts as an abstraction layer for data access.
*   **View:** `MainActivity.kt` manages the main UI, including the song list, navigation drawer, and search. `SongDetailActivity.kt` displays individual song details with zoom support. `SettingsActivity.kt` handles app-level configurations.
*   **ViewModel:** `SongViewModel.kt` prepared and managed data for the UI, communicating exclusively with the `SongRepository`.

**Key Files/Components:**
*   `app/src/main/java/com/guruguhan/lyricsapp/MainActivity.kt`: Main entry point, handles UI setup, search, and manual action mode.
*   `app/src/main/java/com/guruguhan/lyricsapp/viewmodel/SongViewModel.kt`: Manages UI-related data and interacts with `SongRepository`, with robust error handling.
*   `app/src/main/java/com/guruguhan/lyricsapp/data/SongRepository.kt`: Abstraction layer for all data operations.
*   `app/src/main/java/com/guruguhan/lyricsapp/data/Song.kt`: Data class for a song entity (Room entity).
*   `app/src/main/java/com/guruguhan/lyricsapp/ui/SongAdapter.kt`: ListAdapter for displaying songs in a RecyclerView.
*   `app/src/main/java/com/guruguhan/lyricsapp/SongDetailActivity.kt`: Displays song lyrics with pinch-to-zoom and share functionality.
*   `app/src/main/java/com/guruguhan/lyricsapp/SettingsActivity.kt`: Application settings.
*   `app/src/main/java/com/guruguhan/lyricsapp/backup/BackupManager.kt`: Hint at backup functionality.

**Completed Tasks:**
*   **Input Validation:** Added validation to prevent adding new songs with blank titles or lyrics.
*   **Home Page UI:**
    *   Added a Toolbar with a hamburger icon and the app name.
    *   Implemented Navigation Drawer for side panel access.
    *   Centered empty state ("No songs yet") and search result feedback ("No songs found") UI.
*   **YouTube Link:** Added `youtubeLink` support to the data model, database, and UI.
*   **Pinch Zoom Lyrics:** Implemented scaling and fixed panning issues in `SongDetailActivity`.
*   **Edit/Delete Functionality:** Added manual contextual action mode for managing songs.
*   **ViewModel Refactoring:** Ensured `SongViewModel` uses `SongRepository` exclusively.
*   **Error Handling:** Implemented `SharedFlow` for communicating database errors to the UI.
*   **Refine Backup/Restore logic:** Updated `BackupManager` to include `youtubeLink`, made imports additive (non-destructive), and added duplicate detection.
*   **Testing:** Added `SongDaoTest.kt` for verifying database operations.
*   **Carnatic Music Customization (Database):**
    *   Refactored `artist` to `composer`.
    *   Added `deity` field to `Song` entity.
*   **UI Updates:**
    *   Updated Add/Edit dialogs to include "Deity" and renamed "Artist" to "Composer".
    *   Updated `SongDetailActivity` and `SongAdapter` to display these new fields.
*   **Settings Page UI:**
    *   Added the standard top bar with hamburger menu and "Settings" title.
*   **Home Page Organization:**
    *   Implemented grouping/view modes: All Songs, By Deity, By Composer using a ChipGroup.
    *   Added `GroupAdapter` to display lists of unique Deities and Composers.
    *   Implemented drill-down navigation from group lists to filtered song lists.
*   **Material 3 Refinements:**
    *   Enabled Dynamic Color throughout the app for Android 12+ users.
    *   Modernized the "Add Song" dialog by limiting the lyrics field height and making it scrollable.
*   **Selection Feedback:**
    *   Visually highlight the selected item during long-press.
    *   Allow deselecting by tapping the item again.
*   **Fixed:** Lyrics field height in "Add Song" dialog.
*   **UI Simplification:**
    *   Removed "Category" field from the `Song` entity and all related UI components.
    *   Simplified the navigation drawer to include only "Settings" and "Share".
    *   Reverted "Share APK" functionality to a placeholder in both `MainActivity.kt` and `SettingsActivity.kt`.
*   **Full `SettingsActivity` and UI Overhaul:**
    *   Implemented theme switching (Light, Dark, System) via `AppCompatDelegate` and `SharedPreferences`.
    *   Modernized the Settings screen using `MaterialCardView` to group options and `MaterialButton` for actions.
    *   Upgraded the main screen search bar to a `com.google.android.material.textfield.TextInputLayout`.
    *   Replaced the FAB's Holo-era add icon with a modern Material Design vector asset.
    *   Converted song list items to use `MaterialCardView`, with `TextAppearance` for typography and a state-list drawable for improved visual feedback on selection.
*   **Favorites Feature Implementation:**
    *   Added `isFavorite: Boolean` field to `Song` data class.
    *   Implemented database migration (version 3 to 4) to add the `isFavorite` column.
    *   Added `getSongById(id: Int)` and `getFavoriteSongs()` functions to `SongDao`.
    *   Exposed `getSongById` and `favoriteSongs` via `SongRepository` and `SongViewModel`.
    *   Added `toggleFavoriteStatus(song: Song)` to `SongViewModel`.
    *   Refactored `SongDetailActivity` to fetch song by ID via ViewModel and display/toggle favorite status with a new menu item (`ic_star`/`ic_star_border`).
    *   Created `FavoritesActivity` to list all favorited songs.
    *   Added "Favorites" item with `ic_favorite` icon to navigation drawer (`drawer_menu.xml`).
    *   Updated `MainActivity` and `SettingsActivity` to navigate to `FavoritesActivity` via the side panel.
*   **Updated Project Documentation:** Revised `README.md` to be more relevant and up-to-date with the current features and architecture.

**Current To-Do / Areas for Improvement:**
1.   **Fix and Re-implement Deity Suggestions:**
    *   Implemented showing previously added deities in a dropdown when adding or editing a song.
    *   **Note:** The current UI for the dropdown could be further improved.
2.  **Implement Multi-Language Lyrics Support:**
    *   **Goal:** Allow a single song to have lyrics in multiple languages, with a UI to switch between them.
    *   **Data Model:**
        *   Change the `lyrics` field in the `Song` entity from `String` to `Map<String, String>` (Language -> Lyrics Text).
        *   Implement a Room `TypeConverter` to convert the `Map` to/from a JSON string for database storage.
        *   Create a database migration to safely update the existing `lyrics` column to the new format.
    *   **UI/UX:**
        *   **Add/Edit Dialog:** Redesign the dialog to allow users to add multiple language-lyrics pairs (e.g., using a dynamic list or tabbed interface).
        *   **Song Detail View:** Add `Chip`s or `Tab`s to the song detail screen, allowing users to select the language for the lyrics they want to view.
        *   **Navigation:** Add a "Languages" item to the navigation drawer to view all songs grouped by language.
3.  **Implement Share App Link functionality (e.g., Google Play Store link).**
4.  **Explore sharing individual or multiple songs (lyrics and details) to other users of the app (Possible, requires intent filters and data sharing mechanism).**
5.  **Unit/Integration Testing:** Expand test coverage.
6.  **Import song info and lyrics from karnATik website (To do later - due to website parsing complexity)**
# Project TODO

This file lists the tasks that need to be re-implemented after the project was reverted to a previous state.

### Key Features to Re-implement:

- **Edit/Delete Functionality:**
    - [x] Long press on a song item to show "Edit" and "Delete" options in the top menu bar.
    - [x] Implement the logic for the "Edit" option to open the edit song dialog.
    - [x] Implement the logic for the "Delete" option to show a confirmation dialog and then delete the song.

- **UI/UX Enhancements:**
    - [x] Display feedback when no search results are found.
    - [x] Show a message when the song list is empty.
    - [x] Add other placeholder pages in hamburger, settings is just one of those pages.

### Bug Fixes to Re-apply:

- **Pinch-to-Zoom:**
    - [] Fix the pinch-to-zoom functionality in the `SongDetailActivity` to ensure it works correctly. It just scrolls the text, doesn't zoom.

### Refactoring and Architectural Improvements:

- **ViewModel Refactoring:**
    - [x] Ensure `SongViewModel` uses `SongRepository` for all data operations, not `SongDao` directly.

- **Error Handling:**
    - [x] Add `try-catch` blocks for all database operations (`insert`, `update`, `delete`) in the `SongViewModel`.

- **Testing:**
    - [x] Re-create and add the unit tests for `SongDao` in `SongDaoTest.kt`.

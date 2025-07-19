Name: NotePad App

Description: The app is a lightweight and intuitive application for creating, editing, deleting, and
searching notes. It supports multiple users through a login system, and each user can manage
their own private notes. The app also includes reminder functionality for important notes and
ensures users remain logged in until they choose to log out.

Features:
- User Registration & Login
- Secure user-based note storage
- Create, edit, delete notes
- Add reminders to notes using `AlarmManager`
- Search notes by title or content
- Random color assignment for note tiles
- Stay logged in using `SharedPreferences`
- Switch account from dashboard
- Date/time picker for setting reminders

Installation Instructions:
1. Download or clone the repository: https://github.com/SanjidaAfrinRuet/notes.git
2. Open the project in Android Studio
3. Allow Gradle to sync the project
4. Connect a device or open an emulator
5. Run the app

Technologies & Versions Used:
Language: Java
Database: SQLite
Storage: SharedPreferences
Notifications: AlarmManager + BroadcastReceiver
IDE: Android Studio Narwhal (2025.1.1)
Min SDK: 26 (Android 8.0)

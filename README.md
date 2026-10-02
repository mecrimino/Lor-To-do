<<<<<<< HEAD
# LOR TO-DO

<p align="center">
  <strong>Tasks. Local. Yours.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/License-GPL--3.0-blue.svg" alt="License: GPL-3.0" />
  <img src="https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-brightgreen.svg" alt="Android 8.0+" />
  <img src="https://img.shields.io/badge/Internet%20Permission-NONE-success.svg" alt="No Internet Permission" />
  <img src="https://img.shields.io/badge/Language-Kotlin%201.9-purple.svg" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20Material%203-orange.svg" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Database-Room%20(SQLite%20%2B%20FTS4)-red.svg" alt="Room" />
</p>

---

## 1. Vision & Core Principles

**LOR TO-DO** is a blazing-fast, beautiful, offline-by-design to-do and personal task management app for Android. Install it, open it, and start managing tasks in seconds.

- **Zero Setup**: No login, no sign-up, no onboarding friction. First launch opens directly into your task list.
- **100% Offline by Design**: The `android.permission.INTERNET` permission is **never** declared in the manifest. All data resides solely on your physical device.
- **Fast & Responsive**: Cold starts under 1 second; instant task capture under 3 seconds; search results in under 100 ms over 10,000 tasks.
- **Complete Data Ownership**: One-tap export and import in open formats (JSON, CSV, Markdown) via Android's Storage Access Framework.
- **Privacy Guaranteed**: No telemetry, no third-party tracking SDKs, no cloud servers, and no ads.

---

## 2. Key Features

### ⚡ Natural Language Quick-Add (Rule-Based & Local)
Type naturally into the persistent bottom bar and watch it parse attributes in real time with zero network dependencies:
> `"Pay rent tomorrow 6pm !high #bills every month"`
- **Date & Time**: `tomorrow 6pm` → sets tomorrow at 18:00
- **Priority**: `!high`, `!med`, `!low` → assigns priority marker
- **Tags**: `#bills`, `#work`, `#personal` → tags task
- **Recurrence**: `every month`, `daily`, `every week` → schedules recurrence rule

### 📅 Cheerful Date Strip & Smart Views
- **Header & Weekly Date Strip**: Clean horizontal day selector (Mo, Tu, We, Th, Fr, Sa, Su) with active date highlighting.
- **Smart Views**:
  - **Today**: Scheduled tasks and overdue reminders
  - **Inbox**: Quick capture repository
  - **Tomorrow & Upcoming**: 7-day forecast
  - **Overdue**: Critical missed deadlines
  - **Starred**: High-importance pinned items
  - **Completed & Archive**: Completed history and soft-deleted trash

### 📊 Views: Calendar & Kanban
- **Month Calendar Grid**: Day dots indicate task densities; tap any date to inspect and plan.
- **Kanban Board**: Three-stage workflow columns (*To Do*, *In Progress*, *Done*).

### ⏰ Exact Reminders & Housekeeping
- **AlarmManager Exact Alarms**: Reliable alarms surviving Android Doze mode and system restarts via `BootReceiver`.
- **Notification Actions**: Mark **Done** or **Snooze** directly from the notification shade.
- **Trash Retention**: Soft-deleted tasks stay in trash for 30 days before automated background cleanup via `WorkManager`.

### ⏱️ Productivity Tools
- **Pomodoro Focus Timer**: Built-in 25-minute work and 5-minute break cycles attached directly to tasks.
- **Habit Streaks & Statistics**: Daily completion streaks, best streak tracker, and visual distribution charts.

### 🔒 Privacy & Biometric Security
- **Biometric App Lock**: Secure your to-do lists using fingerprint, face unlock, or device PIN.
- **Recents Privacy**: Blur app preview and block screenshots via `FLAG_SECURE`.
- **Sensitive Notification Toggle**: Mask task titles on lock screens.

### 📱 Widgets & System Integration
- **Jetpack Glance Widget**: Resizable home screen widget with interactive task checkboxes and quick add.
- **Quick Settings Tile**: Add tasks instantly from the notification shade.
- **Share Target**: Share highlighted text or links from any browser or app directly into LOR TO-DO.
- **Deep Links**: Full automation support via `lortodo://task/{id}`.

---

## 3. Architecture

```
app/src/main/java/org/lortodo/
├── LorTodoApp.kt                 # Application class & background workers
├── MainActivity.kt               # Single-Activity compose container & intent router
├── core/
│   └── AppContainer.kt           # Dependency Injection container
├── domain/
│   ├── model/                   # Pure Kotlin data classes (Task, Recurrence, Priority)
│   ├── engine/                  # RecurrenceEngine & NaturalLanguageTaskParser
│   └── repository/              # Repository interfaces
├── data/
│   ├── local/                   # Room AppDatabase, DAOs, Entities, and Converters
│   ├── repository/              # Repository implementations
│   └── backup/                  # JSON, CSV, and Markdown BackupManager
├── reminders/                    # AlarmScheduler, ReminderReceiver, BootReceiver
├── service/                      # QuickAddTileService & TrashPurgeWorker
├── widget/                       # Glance AppWidget
└── ui/
    ├── theme/                   # Material 3 Warm Yellow & AMOLED Black Theme
    ├── components/              # DateStrip, TopHeader, TaskItemCard, QuickAddBar
    ├── home/                    # HomeScreen & HomeViewModel
    ├── detail/                  # TaskDetailScreen & TaskDetailViewModel
    ├── calendar/                # CalendarScreen & CalendarViewModel
    ├── kanban/                  # KanbanScreen & KanbanViewModel
    ├── search/                  # SearchScreen & SearchViewModel
    ├── focus/                   # FocusTimerScreen & FocusTimerViewModel
    ├── statistics/              # StatisticsScreen & StatisticsViewModel
    ├── lists/                   # ListsAndTagsScreen & ListsAndTagsViewModel
    ├── trash/                   # TrashArchiveScreen & TrashArchiveViewModel
    ├── settings/                # SettingsScreen, SettingsViewModel & AboutScreen
    └── lock/                    # AppLockScreen (Biometrics)
```

---

## 4. Building from Source

### Prerequisites
- JDK 17
- Android SDK 34 (Android 14)

### Build Commands
```bash
# Clone the repository
git clone https://github.com/<handle>/lor-todo.git
cd lor-todo

# Run Unit Tests
./gradlew testDebugUnitTest

# Build Debug APK
./gradlew assembleDebug

# Build Signed Release APK
./gradlew assembleRelease
```

---

## 5. Privacy Pledge

> **LOR TO-DO** stores all data solely on your device. It requests **no** internet permissions, requires **no** user accounts, serves **no** advertisements, includes **no** third-party trackers, and reports **zero** telemetry. Your tasks never leave your phone unless you choose to export them.

---

## 6. License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**. See the [LICENSE](LICENSE) file for details.
=======
# Lor-To-do
>>>>>>> 9ac3839fdce9f549cbef3650f12add3da04850a4

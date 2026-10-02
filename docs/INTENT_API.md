# LOR TO-DO Intent & Deep Link API

LOR TO-DO exposes local intent endpoints for power users and automation apps (e.g. Tasker, Automate, Shortcuts) without requiring internet or external servers.

## 1. Deep Links

### Open Task by ID
- **URI Scheme:** `lortodo://task/{taskId}`
- **Action:** `android.intent.action.VIEW`
- **Result:** Directly opens the task detail editor for the specified task ID.

## 2. Intent Actions

### Quick Add Task (via Share Sheet)
- **Action:** `android.intent.action.SEND`
- **MIME Type:** `text/plain`
- **Extra:** `Intent.EXTRA_TEXT`
- **Description:** Parses the incoming text with the offline Natural Language Parser and prompts the user or automatically adds the task.

### Launch Actions
- **View Today:**
  ```kotlin
  Intent(context, MainActivity::class.java).apply {
      action = Intent.ACTION_VIEW
      putExtra("action", "view_today")
  }
  ```
- **Open Quick Add Dialog:**
  ```kotlin
  Intent(context, MainActivity::class.java).apply {
      action = Intent.ACTION_VIEW
      putExtra("action", "quick_add")
  }
  ```
- **Open Search:**
  ```kotlin
  Intent(context, MainActivity::class.java).apply {
      action = Intent.ACTION_VIEW
      putExtra("action", "search")
  }
  ```

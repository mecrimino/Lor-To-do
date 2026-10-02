# LOR TO-DO Backup Specification

LOR TO-DO adheres to the principle that **the user fully owns their data**. All exports use open, standard formats that can be parsed, edited, and restored on any platform.

## 1. JSON Backup Format (`.json`)

The primary backup format is a UTF-8 JSON document adhering to the following schema:

```json
{
  "version": 1,
  "app": "LOR-TODO",
  "exportedAt": 1700000000000,
  "tasks": [
    {
      "id": 1,
      "title": "Pay rent",
      "notes": "Include parking fee",
      "listId": 1,
      "priority": "HIGH",
      "isCompleted": false,
      "completedAt": null,
      "dueDate": 1700100000000,
      "dueTime": 64800000,
      "isAllDay": false,
      "isStarred": true,
      "recurrenceRule": {
        "frequency": "MONTHLY",
        "interval": 1,
        "daysOfWeek": [],
        "dayOfMonth": 1,
        "endType": "NEVER",
        "repeatFrom": "DUE_DATE"
      },
      "subtasks": [
        {
          "title": "Check bank balance",
          "isDone": true,
          "sortOrder": 0
        }
      ]
    }
  ],
  "lists": [
    {
      "id": 1,
      "name": "Inbox",
      "colorHex": "#E5A800",
      "icon": "inbox",
      "sortOrder": 0,
      "isArchived": false
    }
  ],
  "tags": [
    {
      "id": 1,
      "name": "bills",
      "colorHex": "#E53935"
    }
  ],
  "taskTagRefs": [
    {
      "taskId": 1,
      "tagId": 1
    }
  ]
}
```

## 2. CSV Format

Exported CSV files use standard comma separation with double-quote escaping:
`Title,Notes,Priority,DueDate,IsCompleted,Subtasks,Tags`

Compatible with Microsoft Excel, Google Sheets, LibreOffice Calc, and third-party task managers.

## 3. Markdown Format

Lists can be exported directly as GitHub Flavored Markdown:

```markdown
# Inbox

- [ ] Pay rent 📅 2026-10-15 !high #bills
  > Include parking fee
  - [x] Check bank balance
- [x] Buy coffee beans
```

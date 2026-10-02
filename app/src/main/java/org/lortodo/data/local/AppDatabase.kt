package org.lortodo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import org.lortodo.data.local.dao.FocusSessionDao
import org.lortodo.data.local.dao.ReminderDao
import org.lortodo.data.local.dao.SavedFilterDao
import org.lortodo.data.local.dao.SubtaskDao
import org.lortodo.data.local.dao.TagDao
import org.lortodo.data.local.dao.TaskDao
import org.lortodo.data.local.dao.TaskListDao
import org.lortodo.data.local.dao.TemplateDao
import org.lortodo.data.local.entity.FocusSessionEntity
import org.lortodo.data.local.entity.ReminderEntity
import org.lortodo.data.local.entity.SavedFilterEntity
import org.lortodo.data.local.entity.SubtaskEntity
import org.lortodo.data.local.entity.TagEntity
import org.lortodo.data.local.entity.TaskEntity
import org.lortodo.data.local.entity.TaskFtsEntity
import org.lortodo.data.local.entity.TaskListEntity
import org.lortodo.data.local.entity.TaskTagCrossRef
import org.lortodo.data.local.entity.TemplateEntity

@Database(
    entities = [
        TaskEntity::class,
        TaskFtsEntity::class,
        SubtaskEntity::class,
        TaskListEntity::class,
        TagEntity::class,
        TaskTagCrossRef::class,
        ReminderEntity::class,
        FocusSessionEntity::class,
        TemplateEntity::class,
        SavedFilterEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun taskListDao(): TaskListDao
    abstract fun tagDao(): TagDao
    abstract fun reminderDao(): ReminderDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun templateDao(): TemplateDao
    abstract fun savedFilterDao(): SavedFilterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lor_todo.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate default lists and tags using raw SQL to avoid deadlock
                        db.execSQL("INSERT INTO task_lists (id, name, colorHex, icon, sortOrder, isArchived) VALUES (1, 'Inbox', '#E5A800', 'inbox', 0, 0)")
                        db.execSQL("INSERT INTO task_lists (id, name, colorHex, icon, sortOrder, isArchived) VALUES (2, 'Personal', '#1E88E5', 'person', 1, 0)")
                        db.execSQL("INSERT INTO task_lists (id, name, colorHex, icon, sortOrder, isArchived) VALUES (3, 'Work', '#43A047', 'work', 2, 0)")
                        db.execSQL("INSERT INTO tags (id, name, colorHex) VALUES (1, 'urgent', '#E53935')")
                        db.execSQL("INSERT INTO tags (id, name, colorHex) VALUES (2, 'important', '#FB8C00')")
                        db.execSQL("INSERT INTO tags (id, name, colorHex) VALUES (3, 'bills', '#8E24AA')")

                        // FTS triggers: keep tasks_fts in sync with tasks (external content FTS4)
                        db.execSQL("""
                            CREATE TRIGGER IF NOT EXISTS tasks_fts_ai AFTER INSERT ON tasks BEGIN
                                INSERT INTO tasks_fts(rowid, title, notes) VALUES (new.id, new.title, new.notes);
                            END
                        """.trimIndent())
                        db.execSQL("""
                            CREATE TRIGGER IF NOT EXISTS tasks_fts_au AFTER UPDATE ON tasks BEGIN
                                INSERT INTO tasks_fts(tasks_fts, rowid, title, notes) VALUES('delete', old.id, old.title, old.notes);
                                INSERT INTO tasks_fts(rowid, title, notes) VALUES (new.id, new.title, new.notes);
                            END
                        """.trimIndent())
                        db.execSQL("""
                            CREATE TRIGGER IF NOT EXISTS tasks_fts_ad AFTER DELETE ON tasks BEGIN
                                INSERT INTO tasks_fts(tasks_fts, rowid, title, notes) VALUES('delete', old.id, old.title, old.notes);
                            END
                        """.trimIndent())
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

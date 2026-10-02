package org.lortodo.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.lortodo.domain.model.RecurrenceRule

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromRecurrenceRule(rule: RecurrenceRule?): String? {
        return rule?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toRecurrenceRule(value: String?): RecurrenceRule? {
        return value?.let {
            try {
                json.decodeFromString<RecurrenceRule>(it)
            } catch (e: Exception) {
                null
            }
        }
    }
}

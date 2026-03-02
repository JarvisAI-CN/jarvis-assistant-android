package com.assistant.voip.data.database.converter

import androidx.room.TypeConverter
import com.assistant.voip.domain.model.TaskStatus

class TaskStatusConverter {

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String {
        return status.name
    }

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus {
        return try {
            TaskStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            TaskStatus.Pending
        }
    }
}

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

class FileTypeConverter {

    @TypeConverter
    fun fromFileType(type: com.assistant.voip.domain.model.FileType): String {
        return type.name
    }

    @TypeConverter
    fun toFileType(value: String): com.assistant.voip.domain.model.FileType {
        return try {
            com.assistant.voip.domain.model.FileType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            com.assistant.voip.domain.model.FileType.OTHER
        }
    }
}

class FileTransferStatusConverter {

    @TypeConverter
    fun fromFileTransferStatus(status: com.assistant.voip.domain.model.FileTransferStatus): String {
        return status.name
    }

    @TypeConverter
    fun toFileTransferStatus(value: String): com.assistant.voip.domain.model.FileTransferStatus {
        return try {
            com.assistant.voip.domain.model.FileTransferStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            com.assistant.voip.domain.model.FileTransferStatus.Pending
        }
    }
}

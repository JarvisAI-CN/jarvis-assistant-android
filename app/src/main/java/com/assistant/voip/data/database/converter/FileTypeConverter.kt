package com.assistant.voip.data.database.converter

import androidx.room.TypeConverter
import com.assistant.voip.domain.model.FileTransferType

class FileTypeConverter {

    @TypeConverter
    fun fromFileType(type: FileTransferType): String {
        return type.name
    }

    @TypeConverter
    fun toFileType(value: String): FileTransferType {
        return try {
            FileTransferType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            FileTransferType.OTHER
        }
    }
}

package com.assistant.voip.data.database.converter

import androidx.room.TypeConverter
import com.assistant.voip.domain.model.FileType

class FileTypeConverter {

    @TypeConverter
    fun fromFileType(type: FileType): String {
        return type.name
    }

    @TypeConverter
    fun toFileType(value: String): FileType {
        return try {
            FileType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            FileType.OTHER
        }
    }
}

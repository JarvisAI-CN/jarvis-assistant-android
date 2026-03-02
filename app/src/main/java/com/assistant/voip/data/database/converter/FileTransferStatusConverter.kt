package com.assistant.voip.data.database.converter

import androidx.room.TypeConverter
import com.assistant.voip.domain.model.FileTransferStatus

class FileTransferStatusConverter {

    @TypeConverter
    fun fromFileTransferStatus(status: FileTransferStatus): String {
        return status.name
    }

    @TypeConverter
    fun toFileTransferStatus(value: String): FileTransferStatus {
        return try {
            FileTransferStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            FileTransferStatus.Pending
        }
    }
}

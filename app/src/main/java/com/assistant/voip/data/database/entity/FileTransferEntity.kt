package com.assistant.voip.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.assistant.voip.domain.model.FileTransferStatus
import com.assistant.voip.domain.model.FileType
import java.util.Date

@Entity(tableName = "file_transfers")
data class FileTransferEntity(
    @PrimaryKey
    val id: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val fileType: FileType,
    val status: FileTransferStatus,
    val progress: Int,
    val uploadSpeed: Long,
    val downloadSpeed: Long,
    val remotePath: String?,
    val createdAt: Date,
    val completedAt: Date?,
    val error: String?,
    val metadata: String
)

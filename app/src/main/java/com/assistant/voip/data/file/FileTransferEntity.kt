package com.assistant.voip.data.file

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.assistant.voip.domain.model.FileTransfer
import com.assistant.voip.domain.model.FileTransferStatus
import com.assistant.voip.domain.model.FileTransferType

@Entity(tableName = "file_transfers")
data class FileTransferEntity(
    @PrimaryKey
    val id: String,
    val fileName: String,
    val fileSize: Long,
    val type: String,
    val status: String,
    val progress: Int,
    val bytesTransferred: Long,
    val totalBytes: Long,
    val createdAt: Long,
    val completedAt: Long?,
    val error: String?
) {
    fun toDomainModel(): FileTransfer {
        return FileTransfer(
            id = id,
            fileName = fileName,
            fileSize = fileSize,
            type = FileTransferType.valueOf(type),
            status = FileTransferStatus.valueOf(status),
            progress = progress,
            bytesTransferred = bytesTransferred,
            totalBytes = totalBytes,
            createdAt = createdAt,
            completedAt = completedAt,
            error = error
        )
    }

    companion object {
        fun fromDomainModel(transfer: FileTransfer): FileTransferEntity {
            return FileTransferEntity(
                id = transfer.id,
                fileName = transfer.fileName,
                fileSize = transfer.fileSize,
                type = transfer.type.name,
                status = transfer.status.name,
                progress = transfer.progress,
                bytesTransferred = transfer.bytesTransferred,
                totalBytes = transfer.totalBytes,
                createdAt = transfer.createdAt,
                completedAt = transfer.completedAt,
                error = transfer.error
            )
        }
    }
}

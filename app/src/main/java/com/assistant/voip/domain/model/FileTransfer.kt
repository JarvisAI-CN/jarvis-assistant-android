package com.assistant.voip.domain.model

enum class FileTransferType {
    DOCUMENT,
    IMAGE,
    VIDEO,
    AUDIO,
    OTHER
}

enum class FileTransferStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELED
}

data class FileTransfer(
    val id: String,
    val fileName: String,
    val fileSize: Long,
    val type: FileTransferType,
    val status: FileTransferStatus,
    val progress: Int,
    val bytesTransferred: Long,
    val totalBytes: Long,
    val createdAt: Long,
    val completedAt: Long?,
    val error: String?
) {
    val progressText: String
        get() = when (status) {
            FileTransferStatus.PENDING -> "准备中"
            FileTransferStatus.IN_PROGRESS -> "$progress%"
            FileTransferStatus.COMPLETED -> "完成"
            FileTransferStatus.FAILED -> "失败"
            FileTransferStatus.CANCELED -> "已取消"
        }

    val statusText: String
        get() = when (status) {
            FileTransferStatus.PENDING -> "准备中"
            FileTransferStatus.IN_PROGRESS -> "传输中"
            FileTransferStatus.COMPLETED -> "完成"
            FileTransferStatus.FAILED -> "失败"
            FileTransferStatus.CANCELED -> "已取消"
        }

    val typeIcon: Int
        get() = when (type) {
            FileTransferType.DOCUMENT -> R.drawable.ic_document
            FileTransferType.IMAGE -> R.drawable.ic_image
            FileTransferType.VIDEO -> R.drawable.ic_video
            FileTransferType.AUDIO -> R.drawable.ic_audio
            FileTransferType.OTHER -> R.drawable.ic_other
        }

    val fileSizeText: String
        get() = when {
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            fileSize < 1024 * 1024 * 1024 -> "${fileSize / (1024 * 1024)} MB"
            else -> "${fileSize / (1024 * 1024 * 1024)} GB"
        }

    val bytesTransferredText: String
        get() = when {
            bytesTransferred < 1024 -> "$bytesTransferred B"
            bytesTransferred < 1024 * 1024 -> "${bytesTransferred / 1024} KB"
            bytesTransferred < 1024 * 1024 * 1024 -> "${bytesTransferred / (1024 * 1024)} MB"
            else -> "${bytesTransferred / (1024 * 1024 * 1024)} GB"
        }

    val totalBytesText: String
        get() = when {
            totalBytes < 1024 -> "$totalBytes B"
            totalBytes < 1024 * 1024 -> "${totalBytes / 1024} KB"
            totalBytes < 1024 * 1024 * 1024 -> "${totalBytes / (1024 * 1024)} MB"
            else -> "${totalBytes / (1024 * 1024 * 1024)} GB"
        }

    val durationText: String
        get() = if (createdAt == 0L) {
            ""
        } else {
            val duration = if (completedAt != null) {
                completedAt - createdAt
            } else {
                System.currentTimeMillis() - createdAt
            }
            val seconds = duration / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            when {
                hours > 0 -> "${hours}h ${minutes % 60}m ${seconds % 60}s"
                minutes > 0 -> "${minutes}m ${seconds % 60}s"
                else -> "${seconds}s"
            }
        }
}

data class FileTransferEvent(
    val transfer: FileTransfer,
    val eventType: FileTransferEventType,
    val oldStatus: FileTransferStatus? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class FileTransferEventType {
    CREATED,
    STATUS_CHANGED,
    PROGRESS_UPDATED,
    COMPLETED,
    FAILED,
    CANCELED
}

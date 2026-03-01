package com.assistant.voip.data.database.dao

import androidx.room.*
import com.assistant.voip.data.database.entity.FileTransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileTransferDao {

    @Query("SELECT * FROM file_transfers ORDER BY createdAt DESC")
    fun getAllFileTransfers(): Flow<List<FileTransferEntity>>

    @Query("SELECT * FROM file_transfers WHERE id = :fileId")
    suspend fun getFileTransferById(fileId: String): FileTransferEntity?

    @Query("SELECT * FROM file_transfers WHERE status = :status ORDER BY createdAt DESC")
    fun getFileTransfersByStatus(status: String): Flow<List<FileTransferEntity>>

    @Query("SELECT * FROM file_transfers WHERE fileType = :fileType ORDER BY createdAt DESC")
    fun getFileTransfersByType(fileType: String): Flow<List<FileTransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFileTransfer(fileTransfer: FileTransferEntity)

    @Update
    suspend fun updateFileTransfer(fileTransfer: FileTransferEntity)

    @Delete
    suspend fun deleteFileTransfer(fileTransfer: FileTransferEntity)

    @Query("DELETE FROM file_transfers WHERE id = :fileId")
    suspend fun deleteFileTransferById(fileId: String)

    @Query("DELETE FROM file_transfers")
    suspend fun deleteAllFileTransfers()

    @Query("SELECT COUNT(*) FROM file_transfers WHERE status = :status")
    suspend fun getFileTransferCountByStatus(status: String): Int

    @Query("SELECT SUM(fileSize) FROM file_transfers WHERE status = 'COMPLETED'")
    suspend fun getTotalTransferredSize(): Long

    @Query("SELECT * FROM file_transfers ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentFileTransfers(limit: Int): Flow<List<FileTransferEntity>>

    @Query("SELECT * FROM file_transfers WHERE status IN ('UPLOADING', 'DOWNLOADING') ORDER BY createdAt DESC")
    fun getActiveTransfers(): Flow<List<FileTransferEntity>>

    @Transaction
    suspend fun updateTransferProgress(fileId: String, progress: Int, status: String, speed: Long) {
        val transfer = getFileTransferById(fileId) ?: return
        val updatedTransfer = transfer.copy(
            progress = progress,
            status = when (status.uppercase()) {
                "COMPLETED" -> com.assistant.voip.domain.model.FileTransferStatus.Completed
                "UPLOADING" -> com.assistant.voip.domain.model.FileTransferStatus.Uploading
                "DOWNLOADING" -> com.assistant.voip.domain.model.FileTransferStatus.Downloading
                "FAILED" -> com.assistant.voip.domain.model.FileTransferStatus.Failed
                else -> com.assistant.voip.domain.model.FileTransferStatus.Pending
            },
            uploadSpeed = if (status.uppercase() == "UPLOADING") speed else 0,
            downloadSpeed = if (status.uppercase() == "DOWNLOADING") speed else 0,
            completedAt = if (status.uppercase() == "COMPLETED") java.util.Date() else null
        )
        updateFileTransfer(updatedTransfer)
    }

    @Query("SELECT * FROM file_transfers WHERE filePath = :filePath LIMIT 1")
    suspend fun getFileTransferByPath(filePath: String): FileTransferEntity?

    @Query("DELETE FROM file_transfers WHERE completedAt < :beforeDate")
    suspend fun deleteOldCompletedTransfers(beforeDate: Long): Int
}

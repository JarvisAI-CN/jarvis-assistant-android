package com.assistant.voip.data.file

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.assistant.voip.data.database.entity.FileTransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileTransferDao {
    @Query("SELECT * FROM file_transfers ORDER BY created_at DESC")
    fun getAllFileTransfers(): List<FileTransferEntity>

    @Query("SELECT * FROM file_transfers WHERE id = :id")
    fun getFileTransferById(id: String): FileTransferEntity?

    @Query("SELECT * FROM file_transfers WHERE status = :status ORDER BY created_at DESC")
    fun getFileTransfersByStatus(status: String): List<FileTransferEntity>

    @Query("SELECT * FROM file_transfers WHERE type = :type ORDER BY created_at DESC")
    fun getFileTransfersByType(type: String): List<FileTransferEntity>

    @Query("SELECT COUNT(*) FROM file_transfers")
    fun getFileTransferCount(): Int

    @Insert
    fun insertFileTransfer(fileTransfer: FileTransferEntity)

    @Update
    fun updateFileTransfer(fileTransfer: FileTransferEntity)

    @Delete
    fun deleteFileTransfer(fileTransfer: FileTransferEntity)

    @Query("DELETE FROM file_transfers WHERE id = :id")
    fun deleteFileTransfer(id: String)

    @Query("DELETE FROM file_transfers")
    fun deleteAllFileTransfers()

    @Query("DELETE FROM file_transfers WHERE status IN ('COMPLETED', 'FAILED', 'CANCELED')")
    fun deleteCompletedFileTransfers()
}

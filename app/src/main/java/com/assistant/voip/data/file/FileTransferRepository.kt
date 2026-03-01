package com.assistant.voip.data.file

import com.assistant.voip.domain.model.FileTransfer

interface FileTransferRepository {
    suspend fun getFileTransfers(): List<FileTransfer>
    suspend fun getFileTransferById(id: String): FileTransfer?
    suspend fun saveFileTransfer(transfer: FileTransfer): FileTransfer
    suspend fun updateFileTransfer(transfer: FileTransfer): FileTransfer
    suspend fun deleteFileTransfer(id: String)
    suspend fun getFileTransfersByStatus(status: String): List<FileTransfer>
    suspend fun clearAllFileTransfers()
}

class FileTransferRepositoryImpl(
    private val fileTransferDao: FileTransferDao
) : FileTransferRepository {
    override suspend fun getFileTransfers(): List<FileTransfer> {
        return fileTransferDao.getAllFileTransfers()
            .map { it.toDomainModel() }
    }

    override suspend fun getFileTransferById(id: String): FileTransfer? {
        return fileTransferDao.getFileTransferById(id)?.toDomainModel()
    }

    override suspend fun saveFileTransfer(transfer: FileTransfer): FileTransfer {
        val entity = FileTransferEntity.fromDomainModel(transfer)
        fileTransferDao.insertFileTransfer(entity)
        return fileTransferDao.getFileTransferById(transfer.id)?.toDomainModel() ?: transfer
    }

    override suspend fun updateFileTransfer(transfer: FileTransfer): FileTransfer {
        val entity = FileTransferEntity.fromDomainModel(transfer)
        fileTransferDao.updateFileTransfer(entity)
        return fileTransferDao.getFileTransferById(transfer.id)?.toDomainModel() ?: transfer
    }

    override suspend fun deleteFileTransfer(id: String) {
        fileTransferDao.deleteFileTransfer(id)
    }

    override suspend fun getFileTransfersByStatus(status: String): List<FileTransfer> {
        return fileTransferDao.getFileTransfersByStatus(status)
            .map { it.toDomainModel() }
    }

    override suspend fun clearAllFileTransfers() {
        fileTransferDao.deleteAllFileTransfers()
    }
}

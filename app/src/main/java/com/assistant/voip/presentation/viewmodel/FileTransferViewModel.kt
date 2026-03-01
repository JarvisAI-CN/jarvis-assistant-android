package com.assistant.voip.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assistant.voip.data.file.FileTransferManager
import com.assistant.voip.domain.model.FileTransfer
import com.assistant.voip.domain.model.FileTransferStatus
import com.assistant.voip.domain.model.FileTransferType
import com.assistant.voip.data.file.FileTransferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FileTransferViewModel(
    private val fileTransferManager: FileTransferManager,
    private val fileTransferRepository: FileTransferRepository
) : ViewModel() {

    private val _fileTransfers = MutableStateFlow<List<FileTransfer>>(emptyList())
    val fileTransfers: StateFlow<List<FileTransfer>> = _fileTransfers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedFile = MutableStateFlow<FileTransfer?>(null)
    val selectedFile: StateFlow<FileTransfer?> = _selectedFile.asStateFlow()

    init {
        loadFileTransfers()
        observeFileTransfers()
    }

    private fun loadFileTransfers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val transfers = fileTransferRepository.getFileTransfers()
                _fileTransfers.value = transfers
            } catch (e: Exception) {
                _errorMessage.value = "加载文件传输记录失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeFileTransfers() {
        fileTransferManager.addFileTransferListener { transfer, oldStatus ->
            viewModelScope.launch {
                if (transfer.status == FileTransferStatus.COMPLETED || 
                    transfer.status == FileTransferStatus.FAILED || 
                    transfer.status == FileTransferStatus.CANCELED) {
                    val existing = _fileTransfers.value.find { it.id == transfer.id }
                    if (existing == null) {
                        _fileTransfers.value = _fileTransfers.value + transfer
                    } else {
                        _fileTransfers.value = _fileTransfers.value.map {
                            if (it.id == transfer.id) transfer else it
                        }
                    }
                } else {
                    val existing = _fileTransfers.value.find { it.id == transfer.id }
                    if (existing == null) {
                        _fileTransfers.value = listOf(transfer) + _fileTransfers.value
                    } else {
                        _fileTransfers.value = _fileTransfers.value.map {
                            if (it.id == transfer.id) transfer else it
                        }
                    }
                }
            }
        }
    }

    fun refreshFiles() {
        loadFileTransfers()
    }

    fun pickFile() {
        // TODO: 实现文件选择逻辑
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // 模拟文件选择
                val testFile = FileTransfer(
                    id = System.currentTimeMillis().toString(),
                    fileName = "test_document.pdf",
                    fileSize = 2_097_152,
                    type = FileTransferType.DOCUMENT,
                    status = FileTransferStatus.PENDING,
                    progress = 0,
                    bytesTransferred = 0,
                    totalBytes = 2_097_152,
                    createdAt = System.currentTimeMillis(),
                    completedAt = null,
                    error = null
                )
                fileTransferManager.uploadFile(testFile)
            } catch (e: Exception) {
                _errorMessage.value = "选择文件失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun openFile(file: FileTransfer) {
        if (file.status == FileTransferStatus.COMPLETED) {
            viewModelScope.launch {
                try {
                    _selectedFile.value = file
                    // TODO: 实现文件打开逻辑
                } catch (e: Exception) {
                    _errorMessage.value = "打开文件失败: ${e.message}"
                }
            }
        } else {
            _errorMessage.value = "文件尚未完成传输"
        }
    }

    fun cancelFileTransfer(file: FileTransfer) {
        if (file.status == FileTransferStatus.PENDING || 
            file.status == FileTransferStatus.IN_PROGRESS) {
            viewModelScope.launch {
                try {
                    fileTransferManager.cancelFileTransfer(file.id)
                } catch (e: Exception) {
                    _errorMessage.value = "取消文件传输失败: ${e.message}"
                }
            }
        } else {
            _errorMessage.value = "该文件传输已完成或已取消"
        }
    }

    fun deleteFileTransfer(file: FileTransfer) {
        viewModelScope.launch {
            try {
                fileTransferRepository.deleteFileTransfer(file.id)
                _fileTransfers.value = _fileTransfers.value.filter { it.id != file.id }
            } catch (e: Exception) {
                _errorMessage.value = "删除文件传输记录失败: ${e.message}"
            }
        }
    }

    fun uploadFile(filePath: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val fileName = filePath.substringAfterLast("/")
                val fileSize = 0 // TODO: 获取实际文件大小
                val fileTransfer = FileTransfer(
                    id = System.currentTimeMillis().toString(),
                    fileName = fileName,
                    fileSize = fileSize,
                    type = FileTransferType.DOCUMENT,
                    status = FileTransferStatus.PENDING,
                    progress = 0,
                    bytesTransferred = 0,
                    totalBytes = fileSize,
                    createdAt = System.currentTimeMillis(),
                    completedAt = null,
                    error = null
                )
                fileTransferManager.uploadFile(fileTransfer)
            } catch (e: Exception) {
                _errorMessage.value = "上传文件失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun downloadFile(fileId: String, fileName: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val fileTransfer = FileTransfer(
                    id = System.currentTimeMillis().toString(),
                    fileName = fileName,
                    fileSize = 0, // TODO: 获取实际文件大小
                    type = FileTransferType.DOCUMENT,
                    status = FileTransferStatus.PENDING,
                    progress = 0,
                    bytesTransferred = 0,
                    totalBytes = 0, // TODO: 获取实际文件大小
                    createdAt = System.currentTimeMillis(),
                    completedAt = null,
                    error = null
                )
                fileTransferManager.downloadFile(fileId, fileTransfer)
            } catch (e: Exception) {
                _errorMessage.value = "下载文件失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSelection() {
        _selectedFile.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

package com.assistant.voip.data.file

import android.content.Context
import android.net.Uri
import android.util.Log
import com.assistant.voip.domain.repository.FileRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.Executors

class FileTransferManager private constructor() {

    companion object {
        private const val TAG = "FileTransferManager"
        @Volatile
        private var instance: FileTransferManager? = null

        fun getInstance(): FileTransferManager {
            return instance ?: synchronized(this) {
                instance ?: FileTransferManager().also { instance = it }
            }
        }
    }

    private val executor = Executors.newFixedThreadPool(3)
    private val repository: FileRepository = FileRepositoryImpl()

    fun uploadFile(fileUri: Uri, callback: FileTransferCallback): Boolean {
        try {
            val context = GlobalAppContext.getContext()
            executor.execute {
                try {
                    callback.onStart()
                    context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                        // 获取文件名
                        val fileName = getFileNameFromUri(context, fileUri)
                        val fileSize = getFileSizeFromUri(context, fileUri)

                        // 创建临时文件
                        val tempFile = File.createTempFile("upload", null, context.cacheDir)
                        FileOutputStream(tempFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }

                        // 上传文件
                        val response = repository.uploadFile(tempFile, fileName, fileSize)

                        if (response.isSuccess) {
                            callback.onProgress(100)
                            callback.onSuccess(response.data)
                            tempFile.delete()
                        } else {
                            callback.onError(response.error ?: "Upload failed")
                            tempFile.delete()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to upload file", e)
                    callback.onError(e.message ?: "Upload failed")
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start file upload", e)
            callback.onError(e.message ?: "Upload failed")
            return false
        }
    }

    fun downloadFile(fileId: String, savePath: File, callback: FileTransferCallback): Boolean {
        try {
            executor.execute {
                try {
                    callback.onStart()
                    val response = repository.downloadFile(fileId)

                    if (response.isSuccess) {
                        response.data?.use { inputStream ->
                            FileOutputStream(savePath).use { outputStream ->
                                val buffer = ByteArray(8192)
                                var bytesRead = 0
                                var totalRead = 0L
                                val totalSize = response.totalSize ?: 0L

                                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                    outputStream.write(buffer, 0, bytesRead)
                                    totalRead += bytesRead
                                    val progress = if (totalSize > 0) {
                                        (totalRead * 100 / totalSize).toInt()
                                    } else {
                                        50
                                    }
                                    callback.onProgress(progress)
                                }

                                callback.onProgress(100)
                                callback.onSuccess(savePath.path)
                            }
                        }
                    } else {
                        callback.onError(response.error ?: "Download failed")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to download file", e)
                    callback.onError(e.message ?: "Download failed")
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start file download", e)
            callback.onError(e.message ?: "Download failed")
            return false
        }
    }

    fun cancelTransfer(fileId: String): Boolean {
        try {
            repository.cancelTransfer(fileId)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel transfer", e)
            return false
        }
    }

    fun getTransferStatus(fileId: String): Single<TransferStatus> {
        return Single.create { emitter ->
            executor.execute {
                try {
                    val status = repository.getTransferStatus(fileId)
                    emitter.onSuccess(status)
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        }
    }

    fun getTransferHistory(): Single<List<TransferHistory>> {
        return Single.create { emitter ->
            executor.execute {
                try {
                    val history = repository.getTransferHistory()
                    emitter.onSuccess(history)
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow("_display_name")
                it.getString(columnIndex)
            } else {
                "unknown"
            }
        } ?: "unknown"
    }

    private fun getFileSizeFromUri(context: Context, uri: Uri): Long {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow("_size")
                it.getLong(columnIndex)
            } else {
                0L
            }
        } ?: 0L
    }
}

interface FileTransferCallback {
    fun onStart()
    fun onProgress(progress: Int)
    fun onSuccess(filePath: String)
    fun onError(error: String)
    fun onCancel()
}

data class TransferStatus(
    val fileId: String,
    val fileName: String,
    val fileSize: Long,
    val transferredSize: Long,
    val status: TransferStatusType,
    val speed: Long,
    val remainingTime: Long
)

enum class TransferStatusType {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    ERROR,
    CANCELED,
    PAUSED
}

data class TransferHistory(
    val id: String,
    val fileName: String,
    val fileSize: Long,
    val transferType: TransferType,
    val status: TransferStatusType,
    val startTime: Long,
    val endTime: Long,
    val speed: Long,
    val error: String? = null
)

enum class TransferType {
    UPLOAD,
    DOWNLOAD
}

data class ApiResponse<T>(
    val isSuccess: Boolean,
    val data: T? = null,
    val error: String? = null,
    val totalSize: Long = 0L
) {
    companion object {
        fun <T> success(data: T?, totalSize: Long = 0L): ApiResponse<T> {
            return ApiResponse(true, data, null, totalSize)
        }

        fun <T> error(error: String): ApiResponse<T> {
            return ApiResponse(false, null, error)
        }
    }
}

class FileRepositoryImpl : FileRepository {
    override fun uploadFile(file: File, fileName: String, fileSize: Long): ApiResponse<String> {
        // 模拟上传过程
        Thread.sleep(5000) // 模拟网络延迟
        return ApiResponse.success(fileName)
    }

    override fun downloadFile(fileId: String): ApiResponse<InputStream> {
        // 模拟下载过程
        Thread.sleep(3000) // 模拟网络延迟
        return ApiResponse.success(null)
    }

    override fun cancelTransfer(fileId: String) {
        // 取消传输实现
    }

    override fun getTransferStatus(fileId: String): TransferStatus {
        return TransferStatus(
            fileId,
            "test.txt",
            1024 * 1024,
            512 * 1024,
            TransferStatusType.IN_PROGRESS,
            1024 * 100,
            5120
        )
    }

    override fun getTransferHistory(): List<TransferHistory> {
        return emptyList()
    }
}

interface FileRepository {
    fun uploadFile(file: File, fileName: String, fileSize: Long): ApiResponse<String>
    fun downloadFile(fileId: String): ApiResponse<InputStream>
    fun cancelTransfer(fileId: String)
    fun getTransferStatus(fileId: String): TransferStatus
    fun getTransferHistory(): List<TransferHistory>
}

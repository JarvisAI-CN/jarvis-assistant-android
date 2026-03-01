package com.assistant.voip.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.assistant.voip.R
import com.assistant.voip.domain.model.CallSession
import com.assistant.voip.domain.model.CallState
import com.assistant.voip.domain.model.FileTransfer
import com.assistant.voip.domain.model.FileTransferStatus
import com.assistant.voip.domain.model.Task
import com.assistant.voip.domain.model.TaskStatus
import com.assistant.voip.presentation.MainActivity
import java.util.concurrent.ConcurrentHashMap

class NotificationService private constructor() {

    companion object {
        private const val TAG = "NotificationService"
        private const val CHANNEL_CALL_ID = "call_notifications"
        private const val CHANNEL_CALL_NAME = "通话通知"
        private const val CHANNEL_MESSAGE_ID = "message_notifications"
        private const val CHANNEL_MESSAGE_NAME = "消息通知"
        private const val CHANNEL_FILE_ID = "file_notifications"
        private const val CHANNEL_FILE_NAME = "文件传输通知"
        private const val CHANNEL_TASK_ID = "task_notifications"
        private const val CHANNEL_TASK_NAME = "任务执行通知"

        private const val NOTIFICATION_CALL_ID = 1001
        private const val NOTIFICATION_MESSAGE_BASE = 2000
        private const val NOTIFICATION_FILE_BASE = 3000
        private const val NOTIFICATION_TASK_BASE = 4000

        @Volatile
        private var instance: NotificationService? = null

        fun getInstance(): NotificationService {
            return instance ?: synchronized(this) {
                instance ?: NotificationService().also { instance = it }
            }
        }
    }

    private val notificationIds = ConcurrentHashMap<String, Int>()
    private var nextMessageId = NOTIFICATION_MESSAGE_BASE
    private var nextFileId = NOTIFICATION_FILE_BASE
    private var nextTaskId = NOTIFICATION_TASK_BASE

    fun initialize(context: Context) {
        createNotificationChannels(context)
        Log.d(TAG, "NotificationService initialized")
    }

    private fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 通话通知渠道
            val callChannel = NotificationChannel(
                CHANNEL_CALL_ID,
                CHANNEL_CALL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "通话相关通知"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(callChannel)

            // 消息通知渠道
            val messageChannel = NotificationChannel(
                CHANNEL_MESSAGE_ID,
                CHANNEL_MESSAGE_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "消息相关通知"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(messageChannel)

            // 文件传输通知渠道
            val fileChannel = NotificationChannel(
                CHANNEL_FILE_ID,
                CHANNEL_FILE_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "文件传输相关通知"
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(fileChannel)

            // 任务执行通知渠道
            val taskChannel = NotificationChannel(
                CHANNEL_TASK_ID,
                CHANNEL_TASK_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "任务执行相关通知"
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(taskChannel)

            Log.d(TAG, "Notification channels created")
        }
    }

    fun showCallNotification(context: Context, session: CallSession) {
        val notificationManager = NotificationManagerCompat.from(context)
        val notification = when (session.state) {
            CallState.CALLING -> createCallingNotification(context, session)
            CallState.CONNECTING -> createConnectingNotification(context, session)
            CallState.CONNECTED -> createConnectedNotification(context, session)
            CallState.ENDED -> createEndedNotification(context, session)
            CallState.FAILED -> createFailedNotification(context, session)
            CallState.RECONNECTING -> createReconnectingNotification(context, session)
            else -> return
        }

        notificationManager.notify(NOTIFICATION_CALL_ID, notification)
        Log.d(TAG, "Call notification shown: ${session.id}")
    }

    fun updateCallNotification(context: Context, session: CallSession) {
        showCallNotification(context, session)
    }

    fun dismissCallNotification(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_CALL_ID)
        Log.d(TAG, "Call notification dismissed")
    }

    fun showMessageNotification(context: Context, title: String, message: String, messageId: String) {
        val notificationId = notificationIds.getOrPut(messageId) { nextMessageId++ }
        val notification = createMessageNotification(context, title, message)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Message notification shown: $messageId")
    }

    fun showFileTransferNotification(context: Context, transfer: FileTransfer) {
        val notificationId = notificationIds.getOrPut(transfer.id) { nextFileId++ }
        val notification = when (transfer.status) {
            FileTransferStatus.PENDING -> createFilePendingNotification(context, transfer)
            FileTransferStatus.IN_PROGRESS -> createFileProgressNotification(context, transfer)
            FileTransferStatus.COMPLETED -> createFileCompletedNotification(context, transfer)
            FileTransferStatus.FAILED -> createFileFailedNotification(context, transfer)
            FileTransferStatus.CANCELED -> createFileCanceledNotification(context, transfer)
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "File transfer notification shown: ${transfer.id}")
    }

    fun updateFileTransferNotification(context: Context, transfer: FileTransfer) {
        showFileTransferNotification(context, transfer)
    }

    fun dismissFileTransferNotification(context: Context, transferId: String) {
        val notificationId = notificationIds[transferId] ?: return
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
        notificationIds.remove(transferId)
        Log.d(TAG, "File transfer notification dismissed: $transferId")
    }

    fun showTaskNotification(context: Context, task: Task) {
        val notificationId = notificationIds.getOrPut(task.id) { nextTaskId++ }
        val notification = when (task.status) {
            TaskStatus.PENDING -> createTaskPendingNotification(context, task)
            TaskStatus.IN_PROGRESS -> createTaskProgressNotification(context, task)
            TaskStatus.COMPLETED -> createTaskCompletedNotification(context, task)
            TaskStatus.FAILED -> createTaskFailedNotification(context, task)
            TaskStatus.CANCELED -> createTaskCanceledNotification(context, task)
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Task notification shown: ${task.id}")
    }

    fun updateTaskNotification(context: Context, task: Task) {
        showTaskNotification(context, task)
    }

    fun dismissTaskNotification(context: Context, taskId: String) {
        val notificationId = notificationIds[taskId] ?: return
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
        notificationIds.remove(taskId)
        Log.d(TAG, "Task notification dismissed: $taskId")
    }

    fun dismissAllNotifications(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancelAll()
        notificationIds.clear()
        Log.d(TAG, "All notifications dismissed")
    }

    private fun createCallingNotification(context: Context, session: CallSession): android.app.Notification {
        val intent = createMainActivityIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_CALL_ID)
            .setSmallIcon(R.drawable.ic_phone)
            .setContentTitle("正在呼叫")
            .setContentText(session.remoteUserName ?: "未知用户")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setShowWhen(true)
            .addAction(R.drawable.ic_hangup, "挂断", createHangupPendingIntent(context))
            .build()
    }

    private fun createConnectingNotification(context: Context, session: CallSession): android.app.Notification {
        val intent = createMainActivityIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_CALL_ID)
            .setSmallIcon(R.drawable.ic_phone)
            .setContentTitle("正在连接")
            .setContentText(session.remoteUserName ?: "未知用户")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setShowWhen(true)
            .setProgress(100, 0, true)
            .addAction(R.drawable.ic_hangup, "挂断", createHangupPendingIntent(context))
            .build()
    }

    private fun createConnectedNotification(context: Context, session: CallSession): android.app.Notification {
        val intent = createMainActivityIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val duration = ((System.currentTimeMillis() - session.startTime) / 1000).toString()
        return NotificationCompat.Builder(context, CHANNEL_CALL_ID)
            .setSmallIcon(R.drawable.ic_phone)
            .setContentTitle("通话中")
            .setContentText("$duration | ${session.remoteUserName ?: "未知用户"}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setShowWhen(true)
            .addAction(R.drawable.ic_mute, "静音", createMutePendingIntent(context))
            .addAction(R.drawable.ic_hangup, "挂断", createHangupPendingIntent(context))
            .build()
    }

    private fun createEndedNotification(context: Context, session: CallSession): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_CALL_ID)
            .setSmallIcon(R.drawable.ic_call_end)
            .setContentTitle("通话已结束")
            .setContentText("通话时长: ${formatDuration(session.duration)}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .build()
    }

    private fun createFailedNotification(context: Context, session: CallSession): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_CALL_ID)
            .setSmallIcon(R.drawable.ic_call_failed)
            .setContentTitle("通话失败")
            .setContentText(session.error ?: "未知错误")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setAutoCancel(true)
            .build()
    }

    private fun createReconnectingNotification(context: Context, session: CallSession): android.app.Notification {
        val intent = createMainActivityIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_CALL_ID)
            .setSmallIcon(R.drawable.ic_phone)
            .setContentTitle("正在重连")
            .setContentText("尝试 ${session.reconnectAttempts}/${3}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setShowWhen(true)
            .setProgress(100, 0, true)
            .build()
    }

    private fun createMessageNotification(context: Context, title: String, message: String): android.app.Notification {
        val intent = createMainActivityIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_MESSAGE_ID)
            .setSmallIcon(R.drawable.ic_message)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()
    }

    private fun createFilePendingNotification(context: Context, transfer: FileTransfer): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_FILE_ID)
            .setSmallIcon(R.drawable.ic_file)
            .setContentTitle("准备传输")
            .setContentText(transfer.fileName)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setShowWhen(true)
            .build()
    }

    private fun createFileProgressNotification(context: Context, transfer: FileTransfer): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_FILE_ID)
            .setSmallIcon(R.drawable.ic_file)
            .setContentTitle("传输中")
            .setContentText("${transfer.fileName} (${transfer.progress}%)")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setShowWhen(true)
            .setProgress(100, transfer.progress, false)
            .build()
    }

    private fun createFileCompletedNotification(context: Context, transfer: FileTransfer): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_FILE_ID)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle("传输完成")
            .setContentText(transfer.fileName)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .build()
    }

    private fun createFileFailedNotification(context: Context, transfer: FileTransfer): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_FILE_ID)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle("传输失败")
            .setContentText("${transfer.fileName}: ${transfer.error}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setAutoCancel(true)
            .build()
    }

    private fun createFileCanceledNotification(context: Context, transfer: FileTransfer): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_FILE_ID)
            .setSmallIcon(R.drawable.ic_cancel)
            .setContentTitle("传输已取消")
            .setContentText(transfer.fileName)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .build()
    }

    private fun createTaskPendingNotification(context: Context, task: Task): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_TASK_ID)
            .setSmallIcon(R.drawable.ic_task)
            .setContentTitle("任务准备中")
            .setContentText(task.title)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setOngoing(true)
            .setShowWhen(true)
            .build()
    }

    private fun createTaskProgressNotification(context: Context, task: Task): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_TASK_ID)
            .setSmallIcon(R.drawable.ic_task)
            .setContentTitle("执行中")
            .setContentText("${task.title} (${task.progress}%)")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setShowWhen(true)
            .setProgress(100, task.progress, false)
            .build()
    }

    private fun createTaskCompletedNotification(context: Context, task: Task): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_TASK_ID)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle("任务完成")
            .setContentText(task.title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .build()
    }

    private fun createTaskFailedNotification(context: Context, task: Task): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_TASK_ID)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle("任务失败")
            .setContentText("${task.title}: ${task.error}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setAutoCancel(true)
            .build()
    }

    private fun createTaskCanceledNotification(context: Context, task: Task): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_TASK_ID)
            .setSmallIcon(R.drawable.ic_cancel)
            .setContentTitle("任务已取消")
            .setContentText(task.title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .build()
    }

    private fun createMainActivityIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
    }

    private fun createHangupPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, CallActionReceiver::class.java).apply {
            action = "ACTION_HANGUP"
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createMutePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, CallActionReceiver::class.java).apply {
            action = "ACTION_MUTE"
        }
        return PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatDuration(duration: Long): String {
        val seconds = duration / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m ${seconds % 60}s"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
}

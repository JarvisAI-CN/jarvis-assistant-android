package com.assistant.voip.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.assistant.voip.presentation.MainActivity
import com.assistant.voip.R

class NotificationManager private constructor() {

    companion object {
        private const val TAG = "NotificationManager"
        private const val CHANNEL_ID = "voip_assistant_channel"
        private const val CHANNEL_NAME = "贾维斯助手通知"
        private const val CHANNEL_DESCRIPTION = "贾维斯助手通知频道"
        private const val NOTIFICATION_ID = 1001

        @Volatile
        private var instance: NotificationManager? = null

        fun getInstance(): NotificationManager {
            return instance ?: synchronized(this) {
                instance ?: NotificationManager().also { instance = it }
            }
        }
    }

    private var notificationManager: android.app.NotificationManager? = null

    fun initialize() {
        try {
            val context = GlobalAppContext.getContext()
            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    enableLights(true)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(100, 200, 300, 400)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }

                notificationManager?.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize NotificationManager", e)
        }
    }

    fun showCallNotification() {
        val context = GlobalAppContext.getContext()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("通话中")
            .setContentText("正在与贾维斯进行通话")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setShowWhen(true)
            .setProgress(0, 0, true)
            .addAction(R.drawable.ic_hangup, "挂断", createHangupPendingIntent())

        notificationManager?.notify(NOTIFICATION_ID, notification.build())
        Log.d(TAG, "Call notification shown")
    }

    fun showMessageNotification(title: String, content: String) {
        val context = GlobalAppContext.getContext()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))

        notificationManager?.notify(System.currentTimeMillis().toInt(), notification.build())
        Log.d(TAG, "Message notification shown")
    }

    fun showFileTransferNotification(
        fileName: String,
        progress: Int,
        status: String
    ) {
        val context = GlobalAppContext.getContext()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("文件传输")
            .setContentText("$status: $fileName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(progress < 100)
            .setShowWhen(true)
            .setProgress(100, progress, false)
            .addAction(R.drawable.ic_cancel, "取消", createCancelTransferPendingIntent())

        notificationManager?.notify(System.currentTimeMillis().toInt(), notification.build())
        Log.d(TAG, "File transfer notification shown")
    }

    fun showTaskNotification(title: String, content: String) {
        val context = GlobalAppContext.getContext()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("任务执行")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setShowWhen(true)

        notificationManager?.notify(System.currentTimeMillis().toInt(), notification.build())
        Log.d(TAG, "Task notification shown")
    }

    fun showErrorNotification(title: String, content: String) {
        val context = GlobalAppContext.getContext()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("错误")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setColor(context.getColor(R.color.error))

        notificationManager?.notify(System.currentTimeMillis().toInt(), notification.build())
        Log.d(TAG, "Error notification shown")
    }

    fun updateCallNotification(duration: String) {
        val context = GlobalAppContext.getContext()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("通话中")
            .setContentText(duration)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setShowWhen(true)
            .addAction(R.drawable.ic_hangup, "挂断", createHangupPendingIntent())

        notificationManager?.notify(NOTIFICATION_ID, notification.build())
        Log.d(TAG, "Call notification updated: $duration")
    }

    fun dismissCallNotification() {
        notificationManager?.cancel(NOTIFICATION_ID)
        Log.d(TAG, "Call notification dismissed")
    }

    fun dismissAllNotifications() {
        notificationManager?.cancelAll()
        Log.d(TAG, "All notifications dismissed")
    }

    private fun createHangupPendingIntent(): PendingIntent {
        val context = GlobalAppContext.getContext()
        val intent = Intent(context, HangupReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createCancelTransferPendingIntent(): PendingIntent {
        val context = GlobalAppContext.getContext()
        val intent = Intent(context, CancelTransferReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class HangupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("HangupReceiver", "Hangup requested")
        // 执行挂断操作
        // 可以调用WebRtcManager的挂断方法
    }
}

class CancelTransferReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("CancelTransferReceiver", "File transfer cancel requested")
        // 取消文件传输
        // 可以调用FileTransferManager的取消方法
    }
}

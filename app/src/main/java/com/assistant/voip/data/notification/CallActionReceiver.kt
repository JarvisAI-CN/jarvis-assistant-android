package com.assistant.voip.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.assistant.voip.data.call.CallManager

class CallActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CallActionReceiver"
        const val ACTION_HANGUP = "ACTION_HANGUP"
        const val ACTION_MUTE = "ACTION_MUTE"
        const val ACTION_UNMUTE = "ACTION_UNMUTE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.w(TAG, "Received null context or intent")
            return
        }

        val action = intent.action
        Log.d(TAG, "Received action: $action")

        when (action) {
            ACTION_HANGUP -> handleHangupAction(context)
            ACTION_MUTE -> handleMuteAction(context)
            ACTION_UNMUTE -> handleUnmuteAction(context)
            else -> Log.w(TAG, "Unknown action: $action")
        }
    }

    private fun handleHangupAction(context: Context) {
        Log.d(TAG, "Handling hangup action")
        try {
            val callManager = CallManager.getInstance()
            val currentSession = callManager.getCurrentCallSession()

            if (currentSession != null) {
                callManager.endCall(currentSession.id)
                    .subscribe({ session ->
                        Log.d(TAG, "Call ended successfully: ${session.id}")
                        val notificationService = NotificationService.getInstance()
                        notificationService.dismissCallNotification(context)
                    }, { error ->
                        Log.e(TAG, "Failed to end call", error)
                    })
            } else {
                Log.w(TAG, "No active call session")
                val notificationService = NotificationService.getInstance()
                notificationService.dismissCallNotification(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling hangup action", e)
        }
    }

    private fun handleMuteAction(context: Context) {
        Log.d(TAG, "Handling mute action")
        try {
            val callManager = CallManager.getInstance()
            val currentSession = callManager.getCurrentCallSession()

            if (currentSession != null) {
                callManager.handleCallAction(com.assistant.voip.domain.model.CallAction.MUTE_AUDIO)
                    .subscribe({ session ->
                        Log.d(TAG, "Audio muted successfully")
                        val notificationService = NotificationService.getInstance()
                        notificationService.updateCallNotification(context, session)
                    }, { error ->
                        Log.e(TAG, "Failed to mute audio", error)
                    })
            } else {
                Log.w(TAG, "No active call session")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling mute action", e)
        }
    }

    private fun handleUnmuteAction(context: Context) {
        Log.d(TAG, "Handling unmute action")
        try {
            val callManager = CallManager.getInstance()
            val currentSession = callManager.getCurrentCallSession()

            if (currentSession != null) {
                callManager.handleCallAction(com.assistant.voip.domain.model.CallAction.MUTE_AUDIO)
                    .subscribe({ session ->
                        Log.d(TAG, "Audio unmuted successfully")
                        val notificationService = NotificationService.getInstance()
                        notificationService.updateCallNotification(context, session)
                    }, { error ->
                        Log.e(TAG, "Failed to unmute audio", error)
                    })
            } else {
                Log.w(TAG, "No active call session")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling unmute action", e)
        }
    }
}

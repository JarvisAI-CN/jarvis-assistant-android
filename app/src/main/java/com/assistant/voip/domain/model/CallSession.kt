package com.assistant.voip.domain.model

enum class CallState {
    IDLE,
    CALLING,
    CONNECTING,
    CONNECTED,
    RINGING,
    ENDED,
    FAILED,
    RECONNECTING
}

enum class CallDirection {
    INCOMING,
    OUTGOING
}

data class CallSession(
    val id: String,
    val state: CallState,
    val direction: CallDirection,
    val startTime: Long,
    val endTime: Long?,
    val duration: Long,
    val remoteUserId: String?,
    val remoteUserName: String?,
    val reconnectAttempts: Int,
    val quality: CallQuality,
    val error: String?
)

data class CallQuality(
    val audioQuality: Float,
    val videoQuality: Float,
    val networkLatency: Long,
    val packetLoss: Float,
    val bitrate: Long
)

enum class CallAction {
    START_CALL,
    ANSWER_CALL,
    END_CALL,
    MUTE_AUDIO,
    MUTE_VIDEO,
    SWITCH_CAMERA,
    HOLD_CALL,
    RESUME_CALL,
    RECONNECT
}

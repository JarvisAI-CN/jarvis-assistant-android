package com.assistant.voip.domain.model

import java.util.Date

data class Call(
    val id: String,
    val contactName: String,
    val contactNumber: String,
    val callType: CallType,
    val callStatus: CallStatus,
    val startTime: Date,
    val endTime: Date?,
    val duration: Long,
    val isRecorded: Boolean,
    val recordingPath: String?,
    val isMuted: Boolean,
    val isSpeakerphone: Boolean,
    val networkQuality: NetworkQuality,
    val audioQuality: AudioQuality,
    val createdAt: Date = Date()
)

enum class CallType {
    Incoming,
    Outgoing,
    Missed,
    Unknown
}

enum class CallStatus {
    Connecting,
    Active,
    Ended,
    Failed,
    Unknown
}

enum class NetworkQuality {
    Excellent,
    Good,
    Fair,
    Poor,
    Unknown
}

enum class AudioQuality {
    High,
    Medium,
    Low,
    Unknown
}

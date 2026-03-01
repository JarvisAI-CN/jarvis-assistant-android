package com.assistant.voip.data.audio

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.assistant.voip.domain.model.AudioSession
import com.assistant.voip.domain.model.AudioQuality
import com.assistant.voip.domain.model.AudioType

@Entity(tableName = "audio_sessions")
data class AudioSessionEntity(
    @PrimaryKey
    val id: String,
    val callId: String,
    val type: String,
    val quality: String,
    val startTime: Long,
    val endTime: Long?,
    val duration: Long,
    val echoCancellationEnabled: Boolean,
    val noiseReductionEnabled: Boolean,
    val autoGainControlEnabled: Boolean,
    val volume: Int,
    val qualityScore: Float
) {
    fun toDomainModel(): AudioSession {
        return AudioSession(
            id = id,
            callId = callId,
            type = AudioType.valueOf(type),
            quality = AudioQuality.valueOf(quality),
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            echoCancellationEnabled = echoCancellationEnabled,
            noiseReductionEnabled = noiseReductionEnabled,
            autoGainControlEnabled = autoGainControlEnabled,
            volume = volume,
            qualityScore = qualityScore
        )
    }

    companion object {
        fun fromDomainModel(session: AudioSession): AudioSessionEntity {
            return AudioSessionEntity(
                id = session.id,
                callId = session.callId,
                type = session.type.name,
                quality = session.quality.name,
                startTime = session.startTime,
                endTime = session.endTime,
                duration = session.duration,
                echoCancellationEnabled = session.echoCancellationEnabled,
                noiseReductionEnabled = session.noiseReductionEnabled,
                autoGainControlEnabled = session.autoGainControlEnabled,
                volume = session.volume,
                qualityScore = session.qualityScore
            )
        }
    }
}

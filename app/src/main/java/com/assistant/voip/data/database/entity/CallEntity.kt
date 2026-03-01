package com.assistant.voip.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.assistant.voip.data.database.converter.DateConverter
import com.assistant.voip.domain.model.CallStatus
import com.assistant.voip.domain.model.CallType
import java.util.Date

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey
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
    val networkQuality: String,
    val audioQuality: String,
    val createdAt: Date = Date()
)

package com.assistant.voip.data.database.converter

import androidx.room.TypeConverter
import com.assistant.voip.domain.model.CallStatus
import com.assistant.voip.domain.model.CallType
import java.util.Date

class DateConverter {

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}

class CallStatusConverter {

    @TypeConverter
    fun fromCallStatus(status: CallStatus): String {
        return status.name
    }

    @TypeConverter
    fun toCallStatus(value: String): CallStatus {
        return try {
            CallStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            CallStatus.Unknown
        }
    }
}

class CallTypeConverter {

    @TypeConverter
    fun fromCallType(type: CallType): String {
        return type.name
    }

    @TypeConverter
    fun toCallType(value: String): CallType {
        return try {
            CallType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            CallType.Unknown
        }
    }
}

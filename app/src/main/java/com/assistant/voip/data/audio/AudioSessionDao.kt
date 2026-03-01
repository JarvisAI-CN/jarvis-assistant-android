package com.assistant.voip.data.audio

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.assistant.voip.data.database.entity.AudioSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioSessionDao {
    @Query("SELECT * FROM audio_sessions ORDER BY start_time DESC")
    fun getAllAudioSessions(): List<AudioSessionEntity>

    @Query("SELECT * FROM audio_sessions WHERE id = :id")
    fun getAudioSessionById(id: String): AudioSessionEntity?

    @Query("SELECT * FROM audio_sessions WHERE call_id = :callId ORDER BY start_time DESC")
    fun getAudioSessionsByCallId(callId: String): List<AudioSessionEntity>

    @Query("SELECT * FROM audio_sessions WHERE type = :type ORDER BY start_time DESC")
    fun getAudioSessionsByType(type: String): List<AudioSessionEntity>

    @Query("SELECT COUNT(*) FROM audio_sessions")
    fun getAudioSessionCount(): Int

    @Insert
    fun insertAudioSession(audioSession: AudioSessionEntity)

    @Update
    fun updateAudioSession(audioSession: AudioSessionEntity)

    @Delete
    fun deleteAudioSession(audioSession: AudioSessionEntity)

    @Query("DELETE FROM audio_sessions WHERE id = :id")
    fun deleteAudioSession(id: String)

    @Query("DELETE FROM audio_sessions")
    fun deleteAllAudioSessions()
}

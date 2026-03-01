package com.assistant.voip.data.audio

import com.assistant.voip.domain.model.AudioSession

interface AudioSessionRepository {
    suspend fun getAudioSessions(): List<AudioSession>
    suspend fun getAudioSessionById(id: String): AudioSession?
    suspend fun saveAudioSession(session: AudioSession): AudioSession
    suspend fun updateAudioSession(session: AudioSession): AudioSession
    suspend fun deleteAudioSession(id: String)
    suspend fun getAudioSessionsByCallId(callId: String): List<AudioSession>
    suspend fun clearAllAudioSessions()
}

class AudioSessionRepositoryImpl(
    private val audioSessionDao: AudioSessionDao
) : AudioSessionRepository {
    override suspend fun getAudioSessions(): List<AudioSession> {
        return audioSessionDao.getAllAudioSessions()
            .map { it.toDomainModel() }
    }

    override suspend fun getAudioSessionById(id: String): AudioSession? {
        return audioSessionDao.getAudioSessionById(id)?.toDomainModel()
    }

    override suspend fun saveAudioSession(session: AudioSession): AudioSession {
        val entity = AudioSessionEntity.fromDomainModel(session)
        audioSessionDao.insertAudioSession(entity)
        return audioSessionDao.getAudioSessionById(session.id)?.toDomainModel() ?: session
    }

    override suspend fun updateAudioSession(session: AudioSession): AudioSession {
        val entity = AudioSessionEntity.fromDomainModel(session)
        audioSessionDao.updateAudioSession(entity)
        return audioSessionDao.getAudioSessionById(session.id)?.toDomainModel() ?: session
    }

    override suspend fun deleteAudioSession(id: String) {
        audioSessionDao.deleteAudioSession(id)
    }

    override suspend fun getAudioSessionsByCallId(callId: String): List<AudioSession> {
        return audioSessionDao.getAudioSessionsByCallId(callId)
            .map { it.toDomainModel() }
    }

    override suspend fun clearAllAudioSessions() {
        audioSessionDao.deleteAllAudioSessions()
    }
}

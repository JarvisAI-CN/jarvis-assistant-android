package com.assistant.voip.domain.usecase

import com.assistant.voip.core.PermissionManager
import com.assistant.voip.domain.model.SpeechRecognitionConfig
import com.assistant.voip.domain.model.SpeechRecognitionResult
import com.assistant.voip.domain.repository.SpeechRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class CheckPermissionsUseCase @Inject constructor(
) {
    operator fun invoke(): Boolean {
        val context = GlobalAppContext.getContext()
        val hasPermissions = PermissionManager.checkRequiredPermissions(context)
        Timber.d("Permissions check result: $hasPermissions")
        return hasPermissions
    }
}

class RequestPermissionsUseCase @Inject constructor(
) {
    operator fun invoke(): Boolean {
        val context = GlobalAppContext.getContext()
        return PermissionManager.requestRequiredPermissions(context as android.app.Activity)
    }
}

class RecognizeSpeechUseCase @Inject constructor(
    private val speechRepository: SpeechRepository
) {
    operator fun invoke(
        config: SpeechRecognitionConfig = SpeechRecognitionConfig()
    ): Flow<SpeechRecognitionResult> = flow {
        try {
            Timber.d("Starting speech recognition with config: $config")

            if (!PermissionManager.hasRecordPermission(GlobalAppContext.getContext())) {
                throw SecurityException("麦克风权限未授权")
            }

            speechRepository.recognizeSpeech(config).collect { result ->
                emit(result)
            }

        } catch (e: Exception) {
            Timber.e(e, "Speech recognition failed")
            throw e
        }
    }
}

class SynthesizeSpeechUseCase @Inject constructor(
    private val speechRepository: SpeechRepository
) {
    suspend operator fun invoke(
        text: String,
        config: com.assistant.voip.domain.model.SpeechSynthesisConfig = com.assistant.voip.domain.model.SpeechSynthesisConfig()
    ): Result<String> {
        return try {
            Timber.d("Starting speech synthesis: $text")
            val result = speechRepository.synthesizeSpeech(text, config)
            Result.success(result)
        } catch (e: Exception) {
            Timber.e(e, "Speech synthesis failed")
            Result.failure(e)
        }
    }
}

class StopSpeechRecognitionUseCase @Inject constructor(
    private val speechRepository: SpeechRepository
) {
    suspend operator fun invoke() {
        try {
            speechRepository.stopRecognition()
            Timber.d("Speech recognition stopped")
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop speech recognition")
            throw e
        }
    }
}

package com.assistant.voip.domain.model

data class SpeechRecognitionConfig(
    val language: String = "zh-CN",
    val sampleRate: Int = 16000,
    val isContinuous: Boolean = false,
    val isOffline: Boolean = false,
    val enablePunctuation: Boolean = true,
    val enablePartialResults: Boolean = true,
    val maxAlternatives: Int = 1,
    val speechTimeoutMs: Int = 30000,
    val wakeWordEnabled: Boolean = false,
    val wakeWord: String = "贾维斯"
)

data class SpeechRecognitionResult(
    val text: String,
    val confidence: Float,
    val isFinal: Boolean,
    val alternatives: List<SpeechAlternative>,
    val timestamp: Long
)

data class SpeechAlternative(
    val text: String,
    val confidence: Float
)

data class SpeechSynthesisConfig(
    val language: String = "zh-CN",
    val voiceType: VoiceType = VoiceType.Female,
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val volume: Float = 1.0f,
    val isOffline: Boolean = false
)

data class SpeechSynthesisRequest(
    val text: String,
    val config: SpeechSynthesisConfig,
    val callback: SpeechSynthesisCallback
)

interface SpeechSynthesisCallback {
    fun onStart()
    fun onProgress(progress: Int)
    fun onComplete(audioFilePath: String)
    fun onError(error: String)
}

enum class VoiceType {
    Male,
    Female,
    Child,
    Robot
}

data class AudioConfig(
    val sampleRate: Int = 16000,
    val channels: Int = 1,
    val bitRate: Int = 128000,
    val codec: AudioCodec = AudioCodec.OPUS,
    val echoCancellation: Boolean = true,
    val noiseSuppression: Boolean = true,
    val autoGainControl: Boolean = true
)

enum class AudioCodec {
    OPUS,
    AAC,
    MP3,
    PCM
}

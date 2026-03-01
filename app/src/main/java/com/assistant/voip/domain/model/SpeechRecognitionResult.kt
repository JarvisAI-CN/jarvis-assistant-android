package com.assistant.voip.domain.model

data class SpeechRecognitionResult(
    val text: String,
    val confidence: Float,
    val sn: Int,
    val total: Int,
    val isFinal: Boolean,
    val error: String?
)

data class SpeechConfig(
    val cuid: String = android.os.Build.MODEL,
    val format: String = "wav",
    val sampleRate: Int = 16000,
    val pid: Int = 1537, // 普通话(支持简单的英文识别)
    val speed: Int = 5, // 语速 0-15
    val pitch: Int = 5, // 音调 0-15
    val volume: Int = 5, // 音量 0-15
    val person: Int = 0 // 发音人 0:女声 1:男声 3:度逍遥 4:度丫丫
) {
    companion object {
        // 语音识别模型
        const val PID_MANDARIN = 1537 // 普通话(支持简单的英文识别)
        const val PID_ENGLISH = 1737 // 英语
        const val PID_CANTONESE = 1637 // 粤语
        const val PID_SICHUANESE = 1837 // 四川话

        // 发音人
        const val PERSON_FEMALE = 0 // 女声
        const val PERSON_MALE = 1 // 男声
        const val PERSON_DU_XIAOYAO = 3 // 度逍遥
        const val PERSON_DU_YAYA = 4 // 度丫丫
        const val PERSON_DUYE = 5003 // 度博文
        const val PERSON_DUXIAOMEI = 5118 // 度小娇
    }
}

enum class SpeechRecognitionState {
    IDLE,
    LISTENING,
    PROCESSING,
    COMPLETED,
    FAILED
}

data class SpeechRecognitionSession(
    val id: String,
    val state: SpeechRecognitionState,
    val text: String,
    val confidence: Float,
    val startTime: Long,
    val endTime: Long?,
    val duration: Long,
    val error: String?
)

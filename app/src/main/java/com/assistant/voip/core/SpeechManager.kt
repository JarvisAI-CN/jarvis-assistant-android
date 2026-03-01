package com.assistant.voip.core

import android.content.Context
import android.util.Log
import com.assistant.voip.BuildConfig
import com.assistant.voip.data.repository.SpeechRepository
import com.assistant.voip.data.repository.impl.BaiduSpeechRepository
import com.assistant.voip.domain.model.SpeechRecognitionConfig
import com.assistant.voip.domain.usecase.RecognizeSpeechUseCase
import com.assistant.voip.domain.usecase.SynthesizeSpeechUseCase
import com.assistant.voip.utils.extensions.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

object SpeechManager {

    // 语音识别配置
    private var speechRecognitionConfig = SpeechRecognitionConfig(
        language = "zh-CN",
        sampleRate = 16000,
        isContinuous = false,
        isOffline = false
    )

    // 语音识别状态
    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> get() = _isRecognizing

    // 语音识别结果
    private val _recognitionResult = MutableStateFlow("")
    val recognitionResult: StateFlow<String> get() = _recognitionResult

    // 语音合成状态
    private val _isSynthesizing = MutableStateFlow(false)
    val isSynthesizing: StateFlow<Boolean> get() = _isSynthesizing

    // 错误信息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    // 语音识别仓库
    private lateinit var speechRepository: SpeechRepository

    // 协程作用域
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 初始化
    fun initialize(context: Context) {
        speechRepository = BaiduSpeechRepository(context)
        Timber.d("SpeechManager initialized")
    }

    // 开始语音识别
    fun startRecognition(onResult: (String) -> Unit = {}) {
        if (_isRecognizing.value) {
            Timber.w("Speech recognition already in progress")
            return
        }

        _isRecognizing.value = true
        _errorMessage.value = null

        scope.launch {
            try {
                Timber.d("Starting speech recognition")

                // 检查权限
                if (!PermissionManager.hasRecordPermission(GlobalAppContext.getContext())) {
                    throw IllegalStateException("麦克风权限未授权")
                }

                // 开始识别
                val result = speechRepository.recognizeSpeech()

                // 更新结果
                _recognitionResult.value = result
                onResult(result)

                Timber.d("Recognition result: $result")

            } catch (e: Exception) {
                Timber.e(e, "Speech recognition error")
                _errorMessage.value = e.message
                GlobalAppContext.getContext().toast("语音识别失败: ${e.message}")
            } finally {
                _isRecognizing.value = false
            }
        }
    }

    // 停止语音识别
    fun stopRecognition() {
        if (!_isRecognizing.value) {
            Timber.w("Speech recognition not in progress")
            return
        }

        scope.launch {
            try {
                speechRepository.stopRecognition()
                _isRecognizing.value = false
                Timber.d("Speech recognition stopped")
            } catch (e: Exception) {
                Timber.e(e, "Stop recognition error")
            }
        }
    }

    // 语音合成
    fun synthesizeSpeech(text: String, onComplete: () -> Unit = {}) {
        if (_isSynthesizing.value) {
            Timber.w("Speech synthesis already in progress")
            return
        }

        _isSynthesizing.value = true
        _errorMessage.value = null

        scope.launch {
            try {
                Timber.d("Starting speech synthesis: $text")

                // 检查权限
                if (!PermissionManager.hasRecordPermission(GlobalAppContext.getContext())) {
                    throw IllegalStateException("音频输出权限未授权")
                }

                speechRepository.synthesizeSpeech(text)
                onComplete()

                Timber.d("Speech synthesis completed")

            } catch (e: Exception) {
                Timber.e(e, "Speech synthesis error")
                _errorMessage.value = e.message
                GlobalAppContext.getContext().toast("语音合成失败: ${e.message}")
            } finally {
                _isSynthesizing.value = false
            }
        }
    }

    // 设置语音识别配置
    fun setRecognitionConfig(config: SpeechRecognitionConfig) {
        speechRecognitionConfig = config
        Timber.d("Recognition config updated: $config")
    }

    // 获取语音识别配置
    fun getRecognitionConfig(): SpeechRecognitionConfig {
        return speechRecognitionConfig
    }

    // 检查语音识别可用性
    fun isRecognitionAvailable(): Boolean {
        return speechRepository.isRecognitionAvailable()
    }

    // 检查语音合成可用性
    fun isSynthesisAvailable(): Boolean {
        return speechRepository.isSynthesisAvailable()
    }

    // 清理资源
    fun cleanup() {
        scope.launch {
            speechRepository.cleanup()
        }
        Timber.d("SpeechManager cleanup")
    }

    // 重置状态
    fun reset() {
        _isRecognizing.value = false
        _isSynthesizing.value = false
        _recognitionResult.value = ""
        _errorMessage.value = null
    }

    // 显示错误
    private fun showError(message: String) {
        Timber.e("Speech error: $message")
        _errorMessage.value = message
        GlobalAppContext.getContext().toast("语音服务错误: $message")
    }

    // 内部错误处理
    private fun handleError(error: Throwable) {
        val message = when (error) {
            is IllegalArgumentException -> "参数错误: ${error.message}"
            is IllegalStateException -> "状态错误: ${error.message}"
            is Exception -> "网络或API错误: ${error.message}"
            else -> "未知错误"
        }
        showError(message)
    }
}

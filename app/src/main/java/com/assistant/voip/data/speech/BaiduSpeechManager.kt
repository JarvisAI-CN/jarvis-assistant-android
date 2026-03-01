package com.assistant.voip.data.speech

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.baidu.speech.asr.SpeechConstant
import com.baidu.speech.asr.SpeechRecognizer
import com.baidu.speech.asr.SpeechRecognizerListener
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

class BaiduSpeechManager private constructor() {

    companion object {
        private const val TAG = "BaiduSpeechManager"
        @Volatile
        private var instance: BaiduSpeechManager? = null

        fun getInstance(): BaiduSpeechManager {
            return instance ?: synchronized(this) {
                instance ?: BaiduSpeechManager().also { instance = it }
            }
        }
    }

    private val executor = Executors.newSingleThreadExecutor()
    private var speechRecognizer: SpeechRecognizer? = null
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var isInitialized = false

    fun initialize(): Boolean {
        if (isInitialized) {
            Log.d(TAG, "BaiduSpeechManager already initialized")
            return true
        }

        try {
            val context = GlobalAppContext.getContext()
            speechRecognizer = SpeechRecognizer.newInstance(context).apply {
                init(SpeechConstant.newBuilder()
                    .setAppId(BuildConfig.BAIDU_APP_ID)
                    .setApiKey(BuildConfig.BAIDU_API_KEY)
                    .setSecretKey(BuildConfig.BAIDU_SECRET_KEY)
                    .setEngineType(SpeechConstant.TYPE_CLOUD)
                    .setLanguage(SpeechConstant.LANGUAGE_ZH)
                    .setSampleRate(16000)
                    .setEncoding(SpeechConstant.ENCODING_PCM_16BIT)
                    .setVad(SpeechConstant.VAD_NORMAL)
                    .setPunctuation(true)
                    .setNumThread(1)
                    .create())
            }

            // 初始化音频录制
            val bufferSize = AudioRecord.getMinBufferSize(
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 4
            )

            isInitialized = true
            Log.d(TAG, "BaiduSpeechManager initialized successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize BaiduSpeechManager", e)
            cleanup()
            return false
        }
    }

    fun startRecognition(callback: SpeechRecognitionCallback): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "BaiduSpeechManager not initialized")
            return false
        }

        if (isRecording) {
            Log.d(TAG, "Recognition already in progress")
            return false
        }

        try {
            isRecording = true

            speechRecognizer?.startListening(object : SpeechRecognizerListener {
                override fun onBeginOfSpeech() {
                    callback.onSpeechStart()
                }

                override fun onEndOfSpeech() {
                    callback.onSpeechEnd()
                }

                override fun onResult(results: MutableList<String>?, error: Int) {
                    if (error == 0 && results != null && results.isNotEmpty()) {
                        callback.onResult(results.joinToString(" "))
                    } else {
                        callback.onError("Recognition error: $error")
                    }
                }

                override fun onError(error: Int) {
                    callback.onError("Recognition error: $error")
                    stopRecognition()
                }

                override fun onVolumeChanged(volume: Int) {
                    callback.onVolumeChange(volume)
                }

                override fun onReady() {
                    callback.onReady()
                }
            })

            // 开始录制
            executor.execute {
                audioRecord?.startRecording()
                val bufferSize = AudioRecord.getMinBufferSize(
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val buffer = ByteArray(bufferSize)

                while (isRecording && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size)
                    if (readSize != null && readSize > 0) {
                        speechRecognizer?.feedAudioData(buffer, readSize)
                    }
                }
            }

            Log.d(TAG, "Recognition started successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recognition", e)
            stopRecognition()
            return false
        }
    }

    fun stopRecognition() {
        try {
            isRecording = false
            audioRecord?.stop()
            speechRecognizer?.stopListening()
            Log.d(TAG, "Recognition stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recognition", e)
        }
    }

    fun synthesizeSpeech(text: String, callback: SpeechSynthesisCallback): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "BaiduSpeechManager not initialized")
            return false
        }

        try {
            executor.execute {
                try {
                    val response = speechRecognizer?.synthesizeSpeech(text,
                        SpeechConstant.newBuilder()
                            .setLanguage(SpeechConstant.LANGUAGE_ZH)
                            .setVoice(SpeechConstant.VOICE_FEMALE)
                            .setSpeed(1.0f)
                            .setPitch(1.0f)
                            .setVolume(1.0f)
                            .create()
                    )

                    if (response?.success == true && response.audioData != null) {
                        callback.onSuccess(response.audioData)
                    } else {
                        callback.onError(response?.errorMessage ?: "Synthesis failed")
                    }
                } catch (e: Exception) {
                    callback.onError(e.message ?: "Synthesis error")
                }
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to synthesize speech", e)
            callback.onError(e.message ?: "Synthesis error")
            return false
        }
    }

    fun cleanup() {
        try {
            stopRecognition()
            audioRecord?.release()
            speechRecognizer?.destroy()
            speechRecognizer = null
            audioRecord = null
            isInitialized = false
            Log.d(TAG, "BaiduSpeechManager cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup BaiduSpeechManager", e)
        }
    }

    fun isInitialized(): Boolean {
        return isInitialized
    }

    fun isRecording(): Boolean {
        return isRecording
    }
}

interface SpeechRecognitionCallback {
    fun onReady()
    fun onSpeechStart()
    fun onSpeechEnd()
    fun onResult(text: String)
    fun onError(error: String)
    fun onVolumeChange(volume: Int)
}

interface SpeechSynthesisCallback {
    fun onSuccess(audioData: ByteArray)
    fun onError(error: String)
    fun onProgress(progress: Int)
}

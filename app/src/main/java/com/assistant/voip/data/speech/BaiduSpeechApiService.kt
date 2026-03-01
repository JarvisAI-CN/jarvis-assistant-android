package com.assistant.voip.data.speech

import android.util.Log
import com.assistant.voip.domain.model.SpeechConfig
import com.assistant.voip.domain.model.SpeechRecognitionResult
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.io.*
import java.net.*
import java.util.concurrent.Executors
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.*

class BaiduSpeechApiService private constructor() {

    companion object {
        private const val TAG = "BaiduSpeechApiService"
        private const val API_KEY = "4hxntHS3cgfJEcIB9gVwJgQf"
        private const val SECRET_KEY = "qhmy73aR8CuM7YzTXFIDTiXdeODKbU1g"
        private const val TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token"
        private const val ASR_URL = "https://vop.baidu.com/server_api"
        private const val TTS_URL = "https://tsn.baidu.com/text2audio"

        @Volatile
        private var instance: BaiduSpeechApiService? = null

        fun getInstance(): BaiduSpeechApiService {
            return instance ?: synchronized(this) {
                instance ?: BaiduSpeechApiService().also { instance = it }
            }
        }
    }

    private val executor = Executors.newSingleThreadExecutor()
    private var accessToken: String? = null
    private var tokenExpireTime: Long = 0

    fun getAccessToken(): Observable<String> {
        return Observable.create { emitter ->
            executor.submit {
                try {
                    // 检查token是否过期
                    if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
                        emitter.onNext(accessToken!!)
                        emitter.onComplete()
                        return@submit
                    }

                    Log.d(TAG, "Fetching new access token")
                    val url = URL("$TOKEN_URL?grant_type=client_credentials&client_id=$API_KEY&client_secret=$SECRET_KEY")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(response)
                        accessToken = json.optString("access_token")
                        val expiresIn = json.optInt("expires_in", 2592000)
                        tokenExpireTime = System.currentTimeMillis() + (expiresIn * 1000L)

                        Log.d(TAG, "Access token obtained, expires in $expiresIn seconds")
                        emitter.onNext(accessToken!!)
                        emitter.onComplete()
                    } else {
                        val error = connection.errorStream.bufferedReader().use { it.readText() }
                        emitter.onError(Exception("Failed to get access token: $error"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting access token", e)
                    emitter.onError(e)
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    fun recognizeAudio(audioData: ByteArray, config: SpeechConfig): Observable<SpeechRecognitionResult> {
        return getAccessToken().flatMap { token ->
            Observable.create { emitter ->
                executor.submit {
                    try {
                        Log.d(TAG, "Starting speech recognition")
                        val url = URL("$ASR_URL?dev_pid=${config.pid}&cuid=${config.cuid}&token=$token")

                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = "POST"
                        connection.doOutput = true
                        connection.setRequestProperty("Content-Type", "audio/${config.format}; rate=${config.sampleRate}")
                        connection.connectTimeout = 10000
                        connection.readTimeout = 10000

                        // 发送音频数据
                        connection.outputStream.use { it.write(audioData) }

                        val responseCode = connection.responseCode
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            val response = connection.inputStream.bufferedReader().use { it.readText() }
                            Log.d(TAG, "Recognition response: $response")

                            val json = JSONObject(response)
                            val errNo = json.optInt("err_no", -1)

                            if (errNo == 0) {
                                val result = json.optJSONArray("result")
                                val sn = json.optInt("sn", 1)
                                val total = json.optInt("total", 1)

                                val resultList = mutableListOf<String>()
                                for (i in 0 until result.length()) {
                                    resultList.add(result.optString(i))
                                }

                                val recognitionResult = SpeechRecognitionResult(
                                    text = resultList.joinToString(""),
                                    confidence = 1.0f,
                                    sn = sn,
                                    total = total,
                                    isFinal = sn == total,
                                    error = null
                                )

                                emitter.onNext(recognitionResult)
                                emitter.onComplete()
                            } else {
                                val errMsg = json.optString("err_msg", "Unknown error")
                                emitter.onError(Exception("Recognition failed ($errNo): $errMsg"))
                            }
                        } else {
                            val error = connection.errorStream.bufferedReader().use { it.readText() }
                            emitter.onError(Exception("HTTP error $responseCode: $error"))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error recognizing audio", e)
                        emitter.onError(e)
                    }
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    fun synthesizeSpeech(text: String, config: SpeechConfig): Observable<ByteArray> {
        return getAccessToken().flatMap { token ->
            Observable.create { emitter ->
                executor.submit {
                    try {
                        Log.d(TAG, "Starting speech synthesis for text: $text")

                        val params = mapOf(
                            "tex" to URLEncoder.encode(text, "UTF-8"),
                            "tok" to token,
                            "cuid" to config.cuid,
                            "ctp" to "1",
                            "lan" to "zh",
                            "spd" to config.speed.toString(),
                            "pit" to config.pitch.toString(),
                            "vol" to config.volume.toString(),
                            "per" to config.person.toString()
                        )

                        val paramString = params.map { "${it.key}=${it.value}" }.joinToString("&")
                        val url = URL("$TTS_URL?$paramString")

                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.connectTimeout = 10000
                        connection.readTimeout = 10000

                        val responseCode = connection.responseCode
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            val contentType = connection.contentType
                            if (contentType?.contains("audio") == true) {
                                val audioData = connection.inputStream.readBytes()
                                Log.d(TAG, "Speech synthesis completed, size: ${audioData.size} bytes")
                                emitter.onNext(audioData)
                                emitter.onComplete()
                            } else {
                                // 返回的是错误信息
                                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                                emitter.onError(Exception("Speech synthesis failed: $error"))
                            }
                        } else {
                            val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                            emitter.onError(Exception("HTTP error $responseCode: $error"))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error synthesizing speech", e)
                        emitter.onError(e)
                    }
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    fun refreshAccessToken(): Observable<Boolean> {
        return Observable.create { emitter ->
            executor.submit {
                try {
                    Log.d(TAG, "Refreshing access token")
                    accessToken = null
                    tokenExpireTime = 0
                    getAccessToken().subscribe(
                        { token ->
                            Log.d(TAG, "Access token refreshed successfully")
                            emitter.onNext(true)
                            emitter.onComplete()
                        },
                        { error ->
                            Log.e(TAG, "Failed to refresh access token", error)
                            emitter.onNext(false)
                            emitter.onComplete()
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error refreshing access token", e)
                    emitter.onNext(false)
                    emitter.onComplete()
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    fun isTokenValid(): Boolean {
        return accessToken != null && System.currentTimeMillis() < tokenExpireTime
    }

    fun clearToken() {
        accessToken = null
        tokenExpireTime = 0
        Log.d(TAG, "Access token cleared")
    }
}

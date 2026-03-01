package com.assistant.voip.data.call

import android.util.Log
import com.assistant.voip.data.rtc.WebRtcManager
import com.assistant.voip.domain.model.CallAction
import com.assistant.voip.domain.model.CallDirection
import com.assistant.voip.domain.model.CallQuality
import com.assistant.voip.domain.model.CallSession
import com.assistant.voip.domain.model.CallState
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.Executors

class CallManager private constructor() {

    companion object {
        private const val TAG = "CallManager"
        private const val MAX_RECONNECT_ATTEMPTS = 3
        private const val RECONNECT_DELAY_MS = 3000L

        @Volatile
        private var instance: CallManager? = null

        fun getInstance(): CallManager {
            return instance ?: synchronized(this) {
                instance ?: CallManager().also { instance = it }
            }
        }
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val webRtcManager = WebRtcManager.getInstance()
    private val callStateSubject = BehaviorSubject.create<CallSession>()

    private var currentCallSession: CallSession? = null
    private var reconnectAttempts = 0

    fun observeCallState(): Observable<CallSession> {
        return callStateSubject.hide()
    }

    fun startCall(remoteUserId: String, remoteUserName: String): Observable<CallSession> {
        return Observable.create { emitter ->
            executor.submit {
                try {
                    val sessionId = System.currentTimeMillis().toString()
                    val session = CallSession(
                        id = sessionId,
                        state = CallState.CALLING,
                        direction = CallDirection.OUTGOING,
                        startTime = System.currentTimeMillis(),
                        endTime = null,
                        duration = 0,
                        remoteUserId = remoteUserId,
                        remoteUserName = remoteUserName,
                        reconnectAttempts = 0,
                        quality = CallQuality(
                            audioQuality = 1.0f,
                            videoQuality = 1.0f,
                            networkLatency = 0,
                            packetLoss = 0f,
                            bitrate = 0
                        ),
                        error = null
                    )

                    currentCallSession = session
                    callStateSubject.onNext(session)
                    emitter.onNext(session)

                    // 初始化WebRTC连接
                    webRtcManager.initialize()
                    val connectedSession = establishConnection(session)
                    callStateSubject.onNext(connectedSession)
                    emitter.onNext(connectedSession)

                    emitter.onComplete()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start call", e)
                    val failedSession = currentCallSession?.copy(
                        state = CallState.FAILED,
                        error = e.message
                    )
                    failedSession?.let {
                        currentCallSession = it
                        callStateSubject.onNext(it)
                        emitter.onNext(it)
                    }
                    emitter.onError(e)
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    fun answerCall(sessionId: String): Observable<CallSession> {
        return Observable.create { emitter ->
            executor.submit {
                try {
                    Log.d(TAG, "Answering call: $sessionId")
                    val session = currentCallSession?.copy(
                        state = CallState.CONNECTING
                    ) ?: throw IllegalStateException("No active call session")

                    callStateSubject.onNext(session)
                    emitter.onNext(session)

                    val connectedSession = establishConnection(session)
                    callStateSubject.onNext(connectedSession)
                    emitter.onNext(connectedSession)

                    emitter.onComplete()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to answer call", e)
                    emitter.onError(e)
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    fun endCall(sessionId: String): Observable<CallSession> {
        return Observable.create { emitter ->
            executor.submit {
                try {
                    Log.d(TAG, "Ending call: $sessionId")
                    val session = currentCallSession ?: throw IllegalStateException("No active call session")

                    // 关闭WebRTC连接
                    webRtcManager.close()

                    val endedSession = session.copy(
                        state = CallState.ENDED,
                        endTime = System.currentTimeMillis(),
                        duration = System.currentTimeMillis() - session.startTime
                    )

                    currentCallSession = null
                    reconnectAttempts = 0
                    callStateSubject.onNext(endedSession)
                    emitter.onNext(endedSession)

                    emitter.onComplete()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to end call", e)
                    emitter.onError(e)
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    fun reconnectCall(sessionId: String): Observable<CallSession> {
        return Observable.create { emitter ->
            executor.submit {
                try {
                    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                        throw IllegalStateException("Max reconnect attempts reached")
                    }

                    Log.d(TAG, "Reconnecting call: $sessionId (attempt ${reconnectAttempts + 1})")
                    val session = currentCallSession?.copy(
                        state = CallState.RECONNECTING,
                        reconnectAttempts = reconnectAttempts + 1
                    ) ?: throw IllegalStateException("No active call session")

                    callStateSubject.onNext(session)
                    emitter.onNext(session)

                    // 等待一段时间后尝试重连
                    Thread.sleep(RECONNECT_DELAY_MS)

                    val reconnectedSession = establishConnection(session)
                    callStateSubject.onNext(reconnectedSession)
                    emitter.onNext(reconnectedSession)

                    emitter.onComplete()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reconnect call", e)
                    val failedSession = currentCallSession?.copy(
                        state = CallState.FAILED,
                        error = e.message
                    )
                    failedSession?.let {
                        currentCallSession = it
                        callStateSubject.onNext(it)
                        emitter.onNext(it)
                    }
                    emitter.onError(e)
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    fun handleCallAction(action: CallAction): Observable<CallSession> {
        return Observable.create { emitter ->
            executor.submit {
                try {
                    val session = currentCallSession ?: throw IllegalStateException("No active call session")
                    val updatedSession = when (action) {
                        CallAction.MUTE_AUDIO -> session.copy(
                            quality = session.quality.copy(audioQuality = 0f)
                        )
                        CallAction.MUTE_VIDEO -> session.copy(
                            quality = session.quality.copy(videoQuality = 0f)
                        )
                        CallAction.HOLD_CALL -> session.copy(
                            state = CallState.CONNECTED
                        )
                        CallAction.RESUME_CALL -> session.copy(
                            state = CallState.CONNECTED
                        )
                        else -> session
                    }

                    currentCallSession = updatedSession
                    callStateSubject.onNext(updatedSession)
                    emitter.onNext(updatedSession)

                    emitter.onComplete()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to handle call action", e)
                    emitter.onError(e)
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    private fun establishConnection(session: CallSession): CallSession {
        return try {
            Log.d(TAG, "Establishing connection for session: ${session.id}")

            // 创建SDP Offer
            val offer = webRtcManager.createOffer().blockingGet()
            Log.d(TAG, "SDP Offer created: ${offer.type}")

            // 模拟连接建立过程
            Thread.sleep(1000)

            session.copy(
                state = CallState.CONNECTED,
                quality = session.quality.copy(
                    networkLatency = 50,
                    packetLoss = 0.01f,
                    bitrate = 128000
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to establish connection", e)
            session.copy(
                state = CallState.FAILED,
                error = e.message
            )
        }
    }

    fun getCurrentCallSession(): CallSession? {
        return currentCallSession
    }

    fun isCallActive(): Boolean {
        return currentCallSession?.state in listOf(
            CallState.CALLING,
            CallState.CONNECTING,
            CallState.CONNECTED,
            CallState.RECONNECTING
        )
    }

    fun clearCallState() {
        currentCallSession = null
        reconnectAttempts = 0
    }
}

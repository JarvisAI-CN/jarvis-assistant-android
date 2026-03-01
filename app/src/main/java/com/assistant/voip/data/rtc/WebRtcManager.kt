package com.assistant.voip.data.rtc

import android.util.Log
import org.webrtc.*
import java.util.concurrent.Executors

class WebRtcManager private constructor() {

    companion object {
        private const val TAG = "WebRtcManager"
        @Volatile
        private var instance: WebRtcManager? = null

        fun getInstance(): WebRtcManager {
            return instance ?: synchronized(this) {
                instance ?: WebRtcManager().also { instance = it }
            }
        }
    }

    private val executor = Executors.newSingleThreadExecutor()
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var audioSource: AudioSource? = null
    private var audioTrack: AudioTrack? = null
    private var videoSource: VideoSource? = null
    private var videoTrack: VideoTrack? = null

    private var isInitialized = false

    fun initialize(): Boolean {
        if (isInitialized) {
            Log.d(TAG, "WebRtcManager already initialized")
            return true
        }

        try {
            // 初始化WebRTC
            PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(
                GlobalAppContext.getContext()
            ).createInitializationOptions())

            peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()

            // 初始化音频源
            audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
            audioTrack = audioSource?.let {
                peerConnectionFactory?.createAudioTrack("audio", it)
            }

            // 初始化视频源（可选）
            videoSource = peerConnectionFactory?.createVideoSource(false)
            videoTrack = videoSource?.let {
                peerConnectionFactory?.createVideoTrack("video", it)
            }

            isInitialized = true
            Log.d(TAG, "WebRtcManager initialized successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize WebRtcManager", e)
            cleanup()
            return false
        }
    }

    fun createPeerConnection(configuration: PeerConnection.RTCConfiguration): PeerConnection? {
        if (!isInitialized) {
            Log.e(TAG, "WebRtcManager not initialized")
            return null
        }

        try {
            peerConnection = peerConnectionFactory?.createPeerConnection(
                configuration,
                object : PeerConnection.Observer {
                    override fun onIceCandidate(candidate: IceCandidate?) {
                        Log.d(TAG, "Received ICE candidate: $candidate")
                    }

                    override fun onDataChannel(dataChannel: DataChannel?) {
                        Log.d(TAG, "Received data channel: $dataChannel")
                    }

                    override fun onIceConnectionReceivingChange(receiving: Boolean) {
                        Log.d(TAG, "ICE connection receiving change: $receiving")
                    }

                    override fun onIceConnectionStateChange(newState: PeerConnection.IceConnectionState?) {
                        Log.d(TAG, "ICE connection state change: $newState")
                    }

                    override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {
                        Log.d(TAG, "ICE gathering state change: $newState")
                    }

                    override fun onSignalingChange(newState: PeerConnection.SignalingState?) {
                        Log.d(TAG, "Signaling state change: $newState")
                    }

                    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {
                        Log.d(TAG, "ICE candidates removed: ${candidates?.joinToString()}")
                    }

                    override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                        Log.d(TAG, "Received track: $receiver")
                    }

                    override fun onRemoveTrack(receiver: RtpReceiver?) {
                        Log.d(TAG, "Removed track: $receiver")
                    }
                }
            )

            // 添加音频轨道
            audioTrack?.let { track ->
                val mediaStream = peerConnectionFactory?.createLocalMediaStream("localStream")
                mediaStream?.addTrack(track)
                peerConnection?.addStream(mediaStream)
            }

            // 添加视频轨道（可选）
            videoTrack?.let { track ->
                val mediaStream = peerConnectionFactory?.createLocalMediaStream("localStream")
                mediaStream?.addTrack(track)
                peerConnection?.addStream(mediaStream)
            }

            Log.d(TAG, "Peer connection created successfully")
            return peerConnection
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create peer connection", e)
            cleanup()
            return null
        }
    }

    fun createOffer(): Single<SessionDescription> {
        return Single.create { emitter ->
            executor.execute {
                try {
                    peerConnection?.createOffer(object : SdpObserver {
                        override fun onCreateSuccess(sdp: SessionDescription?) {
                            sdp?.let {
                                peerConnection?.setLocalDescription(object : SdpObserver {
                                    override fun onCreateSuccess(sdp: SessionDescription?) {}
                                    override fun onSetSuccess() {
                                        emitter.onSuccess(it)
                                    }
                                    override fun onCreateFailure(error: String?) {
                                        emitter.onError(RuntimeException(error))
                                    }
                                    override fun onSetFailure(error: String?) {
                                        emitter.onError(RuntimeException(error))
                                    }
                                }, it)
                            }
                        }

                        override fun onCreateFailure(error: String?) {
                            emitter.onError(RuntimeException(error))
                        }

                        override fun onSetSuccess() {}
                        override fun onSetFailure(error: String?) {
                            emitter.onError(RuntimeException(error))
                        }
                    }, MediaConstraints())
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        }
    }

    fun createAnswer(): Single<SessionDescription> {
        return Single.create { emitter ->
            executor.execute {
                try {
                    peerConnection?.createAnswer(object : SdpObserver {
                        override fun onCreateSuccess(sdp: SessionDescription?) {
                            sdp?.let {
                                peerConnection?.setLocalDescription(object : SdpObserver {
                                    override fun onCreateSuccess(sdp: SessionDescription?) {}
                                    override fun onSetSuccess() {
                                        emitter.onSuccess(it)
                                    }
                                    override fun onCreateFailure(error: String?) {
                                        emitter.onError(RuntimeException(error))
                                    }
                                    override fun onSetFailure(error: String?) {
                                        emitter.onError(RuntimeException(error))
                                    }
                                }, it)
                            }
                        }

                        override fun onCreateFailure(error: String?) {
                            emitter.onError(RuntimeException(error))
                        }

                        override fun onSetSuccess() {}
                        override fun onSetFailure(error: String?) {
                            emitter.onError(RuntimeException(error))
                        }
                    }, MediaConstraints())
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        }
    }

    fun setRemoteDescription(sessionDescription: SessionDescription): Single<Boolean> {
        return Single.create { emitter ->
            executor.execute {
                try {
                    peerConnection?.setRemoteDescription(object : SdpObserver {
                        override fun onCreateSuccess(sdp: SessionDescription?) {}
                        override fun onSetSuccess() {
                            emitter.onSuccess(true)
                        }
                        override fun onCreateFailure(error: String?) {
                            emitter.onError(RuntimeException(error))
                        }
                        override fun onSetFailure(error: String?) {
                            emitter.onError(RuntimeException(error))
                        }
                    }, sessionDescription)
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        }
    }

    fun addIceCandidate(candidate: IceCandidate): Boolean {
        return peerConnection?.addIceCandidate(candidate) ?: false
    }

    fun cleanup() {
        try {
            videoTrack?.dispose()
            videoSource?.dispose()
            audioTrack?.dispose()
            audioSource?.dispose()
            peerConnection?.close()
            peerConnectionFactory?.dispose()
            peerConnection = null
            peerConnectionFactory = null
            isInitialized = false
            Log.d(TAG, "WebRtcManager cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup WebRtcManager", e)
        }
    }

    fun isInitialized(): Boolean {
        return isInitialized
    }
}

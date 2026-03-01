package com.assistant.voip.data.rtc

import org.webrtc.PeerConnection
import org.webrtc.RtpParameters

object WebRtcConfig {

    fun createPeerConnectionConfiguration(): PeerConnection.RTCConfiguration {
        val stunServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
        )

        return PeerConnection.RTCConfiguration(stunServers).apply {
            iceTransportPolicy = PeerConnection.IceTransportPolicy.ALL
            bundlePolicy = PeerConnection.BundlePolicy.BALANCED
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            keyType = PeerConnection.KeyType.ECDSA
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
    }

    fun createAudioConstraints(): MediaConstraints {
        val constraints = MediaConstraints()
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        return constraints
    }

    fun createVideoConstraints(): MediaConstraints {
        val constraints = MediaConstraints()
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        return constraints
    }

    fun createMediaStreamConstraints(): MediaConstraints {
        val constraints = MediaConstraints()
        constraints.mandatory.add(MediaConstraints.KeyValuePair("Audio", "true"))
        constraints.mandatory.add(MediaConstraints.KeyValuePair("Video", "false"))
        return constraints
    }

    fun createRtpParameters(): RtpParameters {
        val parameters = RtpParameters()
        parameters.encodings = listOf(
            RtpParameters.EncodingParameters(
                active = true,
                ssrc = null,
                rid = "audio",
                scaleResolutionDownBy = 1.0,
                maxBitrateBps = 128000,
                minBitrateBps = 64000,
                maxFramerate = 0,
                numTemporalLayers = 1
            )
        )
        return parameters
    }

    fun createAudioOptions(): AudioTrack.AudioOptions {
        return AudioTrack.AudioOptions().apply {
            isEchoCancellation = true
            isAutoGainControl = true
            isNoiseSuppression = true
            isHighpassFilter = true
        }
    }

    fun createVideoOptions(): VideoTrack.VideoOptions {
        return VideoTrack.VideoOptions().apply {
            isVideoProcessingEnabled = true
            isNoiseSuppressionEnabled = true
        }
    }

    const val AUDIO_BITRATE = 128000
    const val VIDEO_BITRATE = 1500000
    const val FRAME_RATE = 30
    const val VIDEO_WIDTH = 1280
    const val VIDEO_HEIGHT = 720

    enum class ConnectionQuality {
        EXCELLENT,
        GOOD,
        FAIR,
        POOR,
        BAD
    }

    fun getConnectionQuality(rtt: Int, jitter: Int, packetLoss: Double): ConnectionQuality {
        return when {
            rtt < 100 && jitter < 30 && packetLoss < 1.0 -> ConnectionQuality.EXCELLENT
            rtt < 200 && jitter < 50 && packetLoss < 3.0 -> ConnectionQuality.GOOD
            rtt < 300 && jitter < 100 && packetLoss < 5.0 -> ConnectionQuality.FAIR
            rtt < 400 && jitter < 150 && packetLoss < 10.0 -> ConnectionQuality.POOR
            else -> ConnectionQuality.BAD
        }
    }

    fun getConnectionQualityDescription(quality: ConnectionQuality): String {
        return when (quality) {
            ConnectionQuality.EXCELLENT -> "网络优秀"
            ConnectionQuality.GOOD -> "网络良好"
            ConnectionQuality.FAIR -> "网络一般"
            ConnectionQuality.POOR -> "网络较差"
            ConnectionQuality.BAD -> "网络极差"
        }
    }
}

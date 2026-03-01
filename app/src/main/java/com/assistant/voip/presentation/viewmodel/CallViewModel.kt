package com.assistant.voip.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assistant.voip.data.call.CallManager
import com.assistant.voip.domain.model.CallSession
import com.assistant.voip.domain.model.CallState
import com.assistant.voip.domain.repository.CallRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CallViewModel(
    private val callManager: CallManager,
    private val callRepository: CallRepository
) : ViewModel() {

    private val _callState = MutableStateFlow<CallState>(CallState.IDLE)
    val callState: StateFlow<CallState> = _callState

    private val _callSession = MutableStateFlow<CallSession?>(null)
    val callSession: StateFlow<CallSession?> = _callSession

    private val _callDuration = MutableStateFlow(0L)
    val callDuration: StateFlow<Long> = _callDuration

    private val _callQuality = MutableStateFlow(100)
    val callQuality: StateFlow<Int> = _callQuality

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val _isSpeakerphoneOn = MutableStateFlow(false)
    val isSpeakerphoneOn: StateFlow<Boolean> = _isSpeakerphoneOn

    fun startCall() {
        viewModelScope.launch {
            try {
                val session = callManager.startCall()
                _callSession.value = session
                _callState.value = CallState.ONGOING
            } catch (e: Exception) {
                _callState.value = CallState.FAILED
            }
        }
    }

    fun endCall() {
        viewModelScope.launch {
            callManager.endCall()
            _callState.value = CallState.IDLE
            _callSession.value = null
            _callDuration.value = 0L
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        callManager.setMute(_isMuted.value)
    }

    fun toggleSpeakerphone() {
        _isSpeakerphoneOn.value = !_isSpeakerphoneOn.value
        callManager.setSpeakerphone(_isSpeakerphoneOn.value)
    }

    fun updateCallDuration(duration: Long) {
        _callDuration.value = duration
    }

    fun updateCallQuality(quality: Int) {
        _callQuality.value = quality
    }

    fun saveCallHistory() {
        _callSession.value?.let { session ->
            viewModelScope.launch {
                callRepository.saveCall(session)
            }
        }
    }

    fun loadCallHistory() {
        viewModelScope.launch {
            val history = callRepository.getCallHistory()
            // 处理通话历史数据
        }
    }

    fun updateCallState(state: CallState) {
        _callState.value = state
    }
}

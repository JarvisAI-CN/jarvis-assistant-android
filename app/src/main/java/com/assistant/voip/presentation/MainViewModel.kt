package com.assistant.voip.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assistant.voip.core.AppStateManager
import com.assistant.voip.core.PermissionManager
import com.assistant.voip.core.SpeechManager
import com.assistant.voip.domain.usecase.CheckPermissionsUseCase
import com.assistant.voip.domain.usecase.RequestPermissionsUseCase
import com.assistant.voip.presentation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val checkPermissionsUseCase: CheckPermissionsUseCase,
    private val requestPermissionsUseCase: RequestPermissionsUseCase
) : ViewModel() {

    // 当前屏幕
    private val _currentScreen = MutableStateFlow(Screen.Main)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // 应用状态
    private val _appState = MutableStateFlow<AppState>(AppState.Loading)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    // 权限状态
    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.Unknown)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    // 网络状态
    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Connecting)
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    // 语音服务状态
    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Ready)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    init {
        initializeViewModel()
    }

    private fun initializeViewModel() {
        viewModelScope.launch {
            // 检查权限状态
            val hasPermissions = checkPermissionsUseCase()
            _permissionState.update {
                if (hasPermissions) PermissionState.Granted else PermissionState.Denied
            }

            // 初始化网络状态
            updateNetworkState()

            // 初始化语音服务
            updateSpeechState()
        }
    }

    fun initializeApp() {
        viewModelScope.launch {
            try {
                // 检查应用状态
                if (!AppStateManager.isInitialized()) {
                    AppStateManager.initializeApp()
                }

                // 检查网络连接
                if (!AppStateManager.isNetworkAvailable()) {
                    _appState.update { AppState.NetworkError }
                    return@launch
                }

                // 初始化语音服务
                SpeechManager.initialize(GlobalAppContext.getContext())

                // 应用状态更新
                _appState.update { AppState.Ready }

                Timber.d("App initialized successfully")

            } catch (e: Exception) {
                Timber.e(e, "App initialization failed")
                _appState.update { AppState.Error(e.message ?: "Unknown error") }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.update { screen }
        Timber.d("Navigated to: $screen")
    }

    fun checkPermissions(): Boolean {
        val hasPermissions = PermissionManager.checkRequiredPermissions(GlobalAppContext.getContext())
        _permissionState.update {
            if (hasPermissions) PermissionState.Granted else PermissionState.Denied
        }
        return hasPermissions
    }

    fun requestPermissions() {
        viewModelScope.launch {
            try {
                requestPermissionsUseCase()
                val hasPermissions = checkPermissions()
                _permissionState.update {
                    if (hasPermissions) PermissionState.Granted else PermissionState.Denied
                }
            } catch (e: Exception) {
                Timber.e(e, "Permission request failed")
                _permissionState.update { PermissionState.Error(e.message ?: "Unknown error") }
            }
        }
    }

    private fun updateNetworkState() {
        viewModelScope.launch {
            val networkState = NetworkManager.getNetworkInfoSummary()
            _networkState.update {
                when {
                    NetworkManager.isNetworkAvailable() -> NetworkState.Connected
                    else -> NetworkState.Disconnected
                }
            }
        }
    }

    private fun updateSpeechState() {
        viewModelScope.launch {
            if (SpeechManager.isRecognitionAvailable()) {
                _speechState.update { SpeechState.Ready }
            } else {
                _speechState.update { SpeechState.Unavailable }
            }
        }
    }

    fun updateAppState(state: AppState) {
        _appState.update { state }
    }

    // 处理屏幕导航
    fun handleNavigation(screen: Screen) {
        when (screen) {
            Screen.Main -> navigateTo(Screen.Main)
            Screen.Call -> navigateTo(Screen.Call)
            Screen.Tasks -> navigateTo(Screen.Tasks)
            Screen.Files -> navigateTo(Screen.Files)
            Screen.Settings -> navigateTo(Screen.Settings)
        }
    }

    // 检查是否可以导航到特定屏幕
    fun canNavigateTo(screen: Screen): Boolean {
        return when (screen) {
            Screen.Call -> _permissionState.value == PermissionState.Granted &&
                    _networkState.value == NetworkState.Connected
            Screen.Tasks -> true
            Screen.Files -> true
            Screen.Settings -> true
            Screen.Main -> true
        }
    }
}

// 应用状态
sealed class AppState {
    object Loading : AppState()
    object Ready : AppState()
    object NetworkError : AppState()
    data class Error(val message: String) : AppState()
}

// 权限状态
sealed class PermissionState {
    object Unknown : PermissionState()
    object Granted : PermissionState()
    object Denied : PermissionState()
    data class Error(val message: String) : PermissionState()
}

// 网络状态
sealed class NetworkState {
    object Connecting : NetworkState()
    object Connected : NetworkState()
    object Disconnected : NetworkState()
}

// 语音状态
sealed class SpeechState {
    object Ready : SpeechState()
    object Initializing : SpeechState()
    object Unavailable : SpeechState()
    data class Error(val message: String) : SpeechState()
}

// 导航屏幕定义
enum class Screen {
    Main,
    Call,
    Tasks,
    Files,
    Settings
}

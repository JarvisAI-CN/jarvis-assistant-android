package com.assistant.voip.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.assistant.voip.utils.extensions.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

object NetworkManager {

    // 网络连接状态
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> get() = _isConnected

    // 网络类型
    private val _networkType = MutableStateFlow(NetworkType.NONE)
    val networkType: StateFlow<NetworkType> get() = _networkType

    // 网络强度
    private val _networkStrength = MutableStateFlow(0)
    val networkStrength: StateFlow<Int> get() = _networkStrength

    // 是否为WiFi
    private val _isWiFi = MutableStateFlow(false)
    val isWiFi: StateFlow<Boolean> get() = _isWiFi

    // 是否为移动网络
    private val _isMobile = MutableStateFlow(false)
    val isMobile: StateFlow<Boolean> get() = _isMobile

    // 是否为VPN
    private val _isVPN = MutableStateFlow(false)
    val isVPN: StateFlow<Boolean> get() = _isVPN

    // 网络延迟
    private val _networkDelay = MutableStateFlow(0)
    val networkDelay: StateFlow<Int> get() = _networkDelay

    // 网络速度
    private val _networkSpeed = MutableStateFlow(0)
    val networkSpeed: StateFlow<Int> get() = _networkSpeed

    // 协程作用域
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 网络管理器
    private lateinit var connectivityManager: ConnectivityManager

    // 网络回调
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            scope.launch {
                updateNetworkState()
                Timber.d("Network available")
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            scope.launch {
                updateNetworkState()
                Timber.d("Network lost")
            }
        }

        override fun onUnavailable() {
            super.onUnavailable()
            scope.launch {
                updateNetworkState()
                Timber.d("Network unavailable")
            }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            scope.launch {
                updateNetworkCapabilities(networkCapabilities)
                Timber.d("Network capabilities changed")
            }
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: android.net.LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            scope.launch {
                updateNetworkProperties(linkProperties)
                Timber.d("Network properties changed")
            }
        }
    }

    // 网络变化广播接收器
    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            scope.launch {
                updateNetworkState()
                Timber.d("Network state changed")
            }
        }
    }

    // 初始化网络管理器
    fun initialize(context: Context) {
        connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        registerNetworkCallback(context)
        registerNetworkReceiver(context)
        updateNetworkState()
        Timber.d("NetworkManager initialized")
    }

    // 注册网络回调
    private fun registerNetworkCallback(context: Context) {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    // 注册网络接收器
    private fun registerNetworkReceiver(context: Context) {
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(networkReceiver, filter)
    }

    // 取消注册
    fun unregister(context: Context) {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            context.unregisterReceiver(networkReceiver)
            Timber.d("NetworkManager unregistered")
        } catch (e: Exception) {
            Timber.e(e, "NetworkManager unregister failed")
        }
    }

    // 更新网络状态
    private fun updateNetworkState() {
        val capabilities = connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)
        }

        _isConnected.value = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (_isConnected.value) {
            updateNetworkCapabilities(capabilities!!)
        } else {
            _networkType.value = NetworkType.NONE
            _isWiFi.value = false
            _isMobile.value = false
            _isVPN.value = false
            GlobalAppContext.getContext().toast("网络连接已断开")
        }
    }

    // 更新网络能力
    private fun updateNetworkCapabilities(capabilities: NetworkCapabilities) {
        _isWiFi.value = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        _isMobile.value = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        _isVPN.value = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)

        _networkType.value = when {
            _isWiFi.value -> NetworkType.WIFI
            _isMobile.value -> NetworkType.MOBILE
            _isVPN.value -> NetworkType.VPN
            else -> NetworkType.NONE
        }

        // 更新网络强度
        if (_isWiFi.value) {
            _networkStrength.value = getWiFiSignalStrength()
        } else if (_isMobile.value) {
            _networkStrength.value = getMobileSignalStrength()
        }
    }

    // 更新网络属性
    private fun updateNetworkProperties(linkProperties: android.net.LinkProperties) {
        // 获取网络速度和延迟信息
        scope.launch(Dispatchers.IO) {
            try {
                // 简单的网络延迟测量
                val delay = measureNetworkDelay()
                _networkDelay.value = delay

                // 网络速度测量（模拟）
                val speed = measureNetworkSpeed()
                _networkSpeed.value = speed

            } catch (e: Exception) {
                Timber.e(e, "Network measurement error")
            }
        }
    }

    // 获取WiFi信号强度
    private fun getWiFiSignalStrength(): Int {
        return try {
            // 获取WiFi信号强度
            75 // 模拟值
        } catch (e: Exception) {
            0
        }
    }

    // 获取移动网络信号强度
    private fun getMobileSignalStrength(): Int {
        return try {
            // 获取移动网络信号强度
            60 // 模拟值
        } catch (e: Exception) {
            0
        }
    }

    // 测量网络延迟
    private suspend fun measureNetworkDelay(): Int {
        return try {
            // 这里可以实现真正的网络延迟测量（ping）
            100 // 模拟值
        } catch (e: Exception) {
            0
        }
    }

    // 测量网络速度
    private suspend fun measureNetworkSpeed(): Int {
        return try {
            // 这里可以实现真正的网络速度测量
            10 // 模拟值 (Mbps)
        } catch (e: Exception) {
            0
        }
    }

    // 检查网络是否可用
    fun isNetworkAvailable(): Boolean {
        return _isConnected.value
    }

    // 检查是否需要重试网络操作
    fun shouldRetryNetworkOperation(): Boolean {
        return _isConnected.value && _networkDelay.value < 500
    }

    // 获取网络连接质量
    fun getNetworkQuality(): NetworkQuality {
        return when {
            !_isConnected.value -> NetworkQuality.DISCONNECTED
            _networkStrength.value < 30 -> NetworkQuality.POOR
            _networkStrength.value < 60 -> NetworkQuality.FAIR
            _networkStrength.value < 80 -> NetworkQuality.GOOD
            else -> NetworkQuality.EXCELLENT
        }
    }

    // 获取网络信息摘要
    fun getNetworkInfoSummary(): String {
        return if (_isConnected.value) {
            "${_networkType.value} (强度: ${_networkStrength.value}%, 延迟: ${_networkDelay.value}ms)"
        } else {
            "未连接"
        }
    }

    enum class NetworkType {
        NONE,
        WIFI,
        MOBILE,
        VPN,
        ETHERNET
    }

    enum class NetworkQuality {
        DISCONNECTED,
        POOR,
        FAIR,
        GOOD,
        EXCELLENT
    }
}

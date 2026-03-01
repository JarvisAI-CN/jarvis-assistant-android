package com.assistant.voip.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.Executors

class NetworkMonitor private constructor() {

    companion object {
        private const val TAG = "NetworkMonitor"
        private const val NETWORK_CHECK_INTERVAL_MS = 5000L

        @Volatile
        private var instance: NetworkMonitor? = null

        fun getInstance(): NetworkMonitor {
            return instance ?: synchronized(this) {
                instance ?: NetworkMonitor().also { instance = it }
            }
        }
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val networkStateSubject = BehaviorSubject.create<NetworkState>()
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun initialize(context: Context) {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        setupNetworkCallback()
        startNetworkMonitoring()
        Log.d(TAG, "NetworkMonitor initialized")
    }

    private fun setupNetworkCallback() {
        val manager = connectivityManager ?: return

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available: $network")
                updateNetworkState()
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost: $network")
                updateNetworkState()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                Log.d(TAG, "Network capabilities changed: $network")
                updateNetworkState()
            }
        }

        manager.registerNetworkCallback(request, networkCallback!!)
    }

    private fun startNetworkMonitoring() {
        executor.submit {
            while (true) {
                try {
                    updateNetworkState()
                    Thread.sleep(NETWORK_CHECK_INTERVAL_MS)
                } catch (e: InterruptedException) {
                    Log.d(TAG, "Network monitoring stopped")
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Error monitoring network", e)
                }
            }
        }
    }

    private fun updateNetworkState() {
        val state = getCurrentNetworkState()
        networkStateSubject.onNext(state)
    }

    fun observeNetworkState(): Observable<NetworkState> {
        return networkStateSubject.hide()
    }

    fun getCurrentNetworkState(): NetworkState {
        val manager = connectivityManager ?: return NetworkState.DISCONNECTED

        val activeNetwork = manager.activeNetwork
        if (activeNetwork == null) {
            return NetworkState.DISCONNECTED
        }

        val capabilities = manager.getNetworkCapabilities(activeNetwork)
        if (capabilities == null) {
            return NetworkState.DISCONNECTED
        }

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                val signalStrength = capabilities.signalStrength ?: -1
                NetworkState.CONNECTED_WIFI(signalStrength)
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val signalStrength = capabilities.signalStrength ?: -1
                NetworkState.CONNECTED_CELLULAR(signalStrength)
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                NetworkState.CONNECTED_ETHERNET
            }
            else -> {
                NetworkState.CONNECTED_OTHER
            }
        }
    }

    fun isNetworkAvailable(): Boolean {
        val state = getCurrentNetworkState()
        return state != NetworkState.DISCONNECTED
    }

    fun isWifiConnected(): Boolean {
        val state = getCurrentNetworkState()
        return state is NetworkState.CONNECTED_WIFI
    }

    fun isCellularConnected(): Boolean {
        val state = getCurrentNetworkState()
        return state is NetworkState.CONNECTED_CELLULAR
    }

    fun getNetworkQuality(): NetworkQuality {
        val state = getCurrentNetworkState()
        return when (state) {
            is NetworkState.CONNECTED_WIFI -> {
                when {
                    state.signalStrength > 70 -> NetworkQuality.EXCELLENT
                    state.signalStrength > 50 -> NetworkQuality.GOOD
                    state.signalStrength > 30 -> NetworkQuality.FAIR
                    else -> NetworkQuality.POOR
                }
            }
            is NetworkState.CONNECTED_CELLULAR -> {
                when {
                    state.signalStrength > 70 -> NetworkQuality.GOOD
                    state.signalStrength > 50 -> NetworkQuality.FAIR
                    else -> NetworkQuality.POOR
                }
            }
            NetworkState.CONNECTED_ETHERNET -> NetworkQuality.EXCELLENT
            NetworkState.CONNECTED_OTHER -> NetworkQuality.FAIR
            NetworkState.DISCONNECTED -> NetworkQuality.NONE
        }
    }

    fun getNetworkType(): NetworkType {
        val state = getCurrentNetworkState()
        return when (state) {
            is NetworkState.CONNECTED_WIFI -> NetworkType.WIFI
            is NetworkState.CONNECTED_CELLULAR -> NetworkType.CELLULAR
            NetworkState.CONNECTED_ETHERNET -> NetworkType.ETHERNET
            NetworkState.CONNECTED_OTHER -> NetworkType.OTHER
            NetworkState.DISCONNECTED -> NetworkType.NONE
        }
    }

    fun cleanup() {
        networkCallback?.let {
            connectivityManager?.unregisterNetworkCallback(it)
        }
        networkCallback = null
        Log.d(TAG, "NetworkMonitor cleaned up")
    }
}

sealed class NetworkState {
    data class CONNECTED_WIFI(val signalStrength: Int) : NetworkState()
    data class CONNECTED_CELLULAR(val signalStrength: Int) : NetworkState()
    object CONNECTED_ETHERNET : NetworkState()
    object CONNECTED_OTHER : NetworkState()
    object DISCONNECTED : NetworkState()
}

enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    NONE
}

enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    OTHER,
    NONE
}

class NetworkOptimizer private constructor() {

    companion object {
        private const val TAG = "NetworkOptimizer"
        private const val LATENCY_THRESHOLD_MS = 200L
        private const val PACKET_LOSS_THRESHOLD = 0.05f

        @Volatile
        private var instance: NetworkOptimizer? = null

        fun getInstance(): NetworkOptimizer {
            return instance ?: synchronized(this) {
                instance ?: NetworkOptimizer().also { instance = it }
            }
        }
    }

    private val networkMonitor = NetworkMonitor.getInstance()
    private var lastNetworkQuality = NetworkQuality.NONE

    fun optimizeForNetwork(): NetworkOptimization {
        val quality = networkMonitor.getNetworkQuality()
        val type = networkMonitor.getNetworkType()
        val state = networkMonitor.getCurrentNetworkState()

        return NetworkOptimization(
            networkType = type,
            networkQuality = quality,
            recommendedBitrate = getRecommendedBitrate(quality, type),
            enableAdaptiveBitrate = quality != NetworkQuality.EXCELLENT,
            enableFEC = quality == NetworkQuality.POOR,
            recommendedPacketSize = getRecommendedPacketSize(quality),
            enableCompression = quality != NetworkQuality.EXCELLENT,
            recommendedRetryCount = getRecommendedRetryCount(quality)
        )
    }

    fun getNetworkStatistics(): NetworkStatistics {
        val state = networkMonitor.getCurrentNetworkState()
        val quality = networkMonitor.getNetworkQuality()

        return NetworkStatistics(
            networkType = networkMonitor.getNetworkType(),
            networkQuality = quality,
            signalStrength = when (state) {
                is NetworkState.CONNECTED_WIFI -> state.signalStrength
                is NetworkState.CONNECTED_CELLULAR -> state.signalStrength
                else -> -1
            },
            isConnected = networkMonitor.isNetworkAvailable(),
            isWifi = networkMonitor.isWifiConnected(),
            isCellular = networkMonitor.isCellularConnected()
        )
    }

    private fun getRecommendedBitrate(quality: NetworkQuality, type: NetworkType): Long {
        return when (quality) {
            NetworkQuality.EXCELLENT -> when (type) {
                NetworkType.WIFI, NetworkType.ETHERNET -> 2000000 // 2 Mbps
                NetworkType.CELLULAR -> 1000000 // 1 Mbps
                else -> 500000 // 500 Kbps
            }
            NetworkQuality.GOOD -> 1000000 // 1 Mbps
            NetworkQuality.FAIR -> 500000 // 500 Kbps
            NetworkQuality.POOR -> 250000 // 250 Kbps
            NetworkQuality.NONE -> 0
        }
    }

    private fun getRecommendedPacketSize(quality: NetworkQuality): Int {
        return when (quality) {
            NetworkQuality.EXCELLENT -> 1400
            NetworkQuality.GOOD -> 1200
            NetworkQuality.FAIR -> 1000
            NetworkQuality.POOR -> 800
            NetworkQuality.NONE -> 0
        }
    }

    private fun getRecommendedRetryCount(quality: NetworkQuality): Int {
        return when (quality) {
            NetworkQuality.EXCELLENT -> 0
            NetworkQuality.GOOD -> 1
            NetworkQuality.FAIR -> 2
            NetworkQuality.POOR -> 3
            NetworkQuality.NONE -> 0
        }
    }
}

data class NetworkOptimization(
    val networkType: NetworkType,
    val networkQuality: NetworkQuality,
    val recommendedBitrate: Long,
    val enableAdaptiveBitrate: Boolean,
    val enableFEC: Boolean,
    val recommendedPacketSize: Int,
    val enableCompression: Boolean,
    val recommendedRetryCount: Int
)

data class NetworkStatistics(
    val networkType: NetworkType,
    val networkQuality: NetworkQuality,
    val signalStrength: Int,
    val isConnected: Boolean,
    val isWifi: Boolean,
    val isCellular: Boolean
)

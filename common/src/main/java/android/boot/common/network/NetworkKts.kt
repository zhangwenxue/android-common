package android.boot.common.network

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.boot.common.extensions.i
import android.boot.common.provider.globalContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow

sealed class NetworkConnection(val name: String, val rssi: Float) {
    data object None : NetworkConnection("NONE", 0f)
    data object Wifi : NetworkConnection("WIFI", 1f)
    data object Cellular : NetworkConnection("CELLULAR", 1f)
    data object Other : NetworkConnection("OTHER", .8f)
}

val NetworkConnection.isConnected: Boolean
    get() {
        return this !is NetworkConnection.None
    }

private val networkRequest = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
    .build()

fun networkConnectionChangeFlow() = callbackFlow {
    val connectivityManager = globalContext.getSystemService(ConnectivityManager::class.java)
    val initialNetworkType = connectedNetworkType()
    var isConnected = initialNetworkType.isConnected
    trySendBlocking(initialNetworkType)
    val callback = object : NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            "onAvailable,$network".i("_network")
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
            "onLosing,$network-$maxMsToLive".i("_network")
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            "onLost,$network".i("_network")
            val connection = connectedNetworkType()
            if (isConnected != connection.isConnected) {
                trySendBlocking(connection)
            }
            isConnected = connection.isConnected
        }

        override fun onUnavailable() {
            super.onUnavailable()
            "onUnavailable".i("_network")
            val connection = connectedNetworkType()
            if (isConnected != connection.isConnected) {
                trySendBlocking(connection)
            }
            isConnected = connection.isConnected
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            "onCapabilitiesChanged,$network\n$networkCapabilities".i("_network")
            val connection = connectedNetworkType()
            if (isConnected != connection.isConnected) {
                trySendBlocking(connection)
            }
            isConnected = connection.isConnected
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            "onLinkPropertiesChanged,$network\n$linkProperties".i("_network")
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            super.onBlockedStatusChanged(network, blocked)
            "onBlockedStatusChanged,$network-$blocked".i("_network")
        }
    }
    "registerNetworkCallback".i("_network")
    connectivityManager.registerNetworkCallback(networkRequest, callback)
    awaitClose {
        "unRegisterNetworkCallback".i("_network")
        connectivityManager.unregisterNetworkCallback(callback)
    }
}


fun isNetworkConnected(): Boolean {
    val connectivityManager = globalContext.getSystemService(ConnectivityManager::class.java)
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

fun connectedNetworkType(): NetworkConnection {
    val connectivityManager = globalContext.getSystemService(ConnectivityManager::class.java)
    val network = connectivityManager.activeNetwork ?: return NetworkConnection.None
    val capabilities =
        connectivityManager.getNetworkCapabilities(network) ?: return NetworkConnection.None
    if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) return NetworkConnection.None
    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkConnection.Cellular
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkConnection.Wifi
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> NetworkConnection.Other
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkConnection.Other
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkConnection.Other
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) -> NetworkConnection.Other
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) -> NetworkConnection.Other
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_USB) -> NetworkConnection.Other
        else -> NetworkConnection.None
    }
}
package com.quiz.pride.managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

/**
 * Manages network connectivity state and provides reactive updates.
 * El [networkState] es un StateFlow compartido — registra UN SOLO NetworkCallback
 * con el OS independientemente de cuantos observadores haya.
 */
class NetworkManager(
    private val context: Context,
    applicationScope: CoroutineScope
) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Check if the device is currently connected to the internet
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * StateFlow compartido del estado de red.
     * Un unico NetworkCallback registrado con el OS; multiples observadores
     * comparten la misma suscripcion gracias a WhileSubscribed(5000).
     */
    val networkState: StateFlow<NetworkState> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkState.Available)
            }

            override fun onLost(network: Network) {
                trySend(NetworkState.Unavailable)
            }

            override fun onUnavailable() {
                trySend(NetworkState.Unavailable)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                if (hasInternet && isValidated) {
                    trySend(NetworkState.Available)
                } else {
                    trySend(NetworkState.Unavailable)
                }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Emitir estado inicial
        trySend(if (isNetworkAvailable()) NetworkState.Available else NetworkState.Unavailable)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .distinctUntilChanged()
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = if (isNetworkAvailable()) NetworkState.Available else NetworkState.Unavailable
        )

    /**
     * Get the current connection type
     */
    fun getConnectionType(): ConnectionType {
        val network = connectivityManager.activeNetwork ?: return ConnectionType.None
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ConnectionType.None

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.Wifi
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.Cellular
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.Ethernet
            else -> ConnectionType.Other
        }
    }
}

/**
 * Represents the current network state
 */
sealed class NetworkState {
    data object Available : NetworkState()
    data object Unavailable : NetworkState()
}

/**
 * Represents the type of network connection
 */
sealed class ConnectionType {
    data object Wifi : ConnectionType()
    data object Cellular : ConnectionType()
    data object Ethernet : ConnectionType()
    data object Other : ConnectionType()
    data object None : ConnectionType()
}

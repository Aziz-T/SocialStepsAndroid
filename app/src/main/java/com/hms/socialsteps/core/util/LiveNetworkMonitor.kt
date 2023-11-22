package com.hms.socialsteps.core.util

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import timber.log.Timber
import javax.inject.Inject

class LiveNetworkMonitor
@Inject constructor(
    private val connectivityManager: ConnectivityManager
): LiveData<Boolean>(){
    companion object{
        const val TAG = "LiveNetworkMonitor"
    }

    private lateinit var networkCallback: NetworkCallback

    private val networksThatHaveInternet: MutableSet<Network> = HashSet()

    init {
        checkIfOneNetworkHasInternet()
    }

    private fun checkIfOneNetworkHasInternet() {
        postValue(networksThatHaveInternet.size > 0)
    }

    override fun onActive() {
        networkCallback = createNetworkCallback()

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onInactive() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun createNetworkCallback() = object: NetworkCallback(){
        override fun onAvailable(network: Network) {
            Timber.tag(TAG).i( "onAvailable: Device got connection to $network")

            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            val hasInternet = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)

            if (hasInternet == true)
                networksThatHaveInternet.add(network)

            checkIfOneNetworkHasInternet()
        }

        override fun onLost(network: Network) {
            Timber.tag(TAG).i( "onLost: Device lost connection to $network")

            networksThatHaveInternet.remove(network)

            checkIfOneNetworkHasInternet()
        }
    }



}
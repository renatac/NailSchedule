package com.example.nailschedule.view.activities.view.gallery

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GalleryViewModel : ViewModel() {
    var hasPhoto = MutableLiveData<Boolean>()

    private var _hasInternet = MutableLiveData<Pair<Boolean, String>>()
    val hasInternet: LiveData<Pair<Boolean, String>>
    get() = _hasInternet

    fun checkForInternet(context: Context, str: String) {
        // register activity with the connectivity manager service
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Returns a Network object corresponding to
            // the currently active default data network.
            connectivityManager.activeNetwork?.let {
                val network = connectivityManager.activeNetwork

                // Representation of the capabilities of an active network.
                connectivityManager.getNetworkCapabilities(network)?.let {
                    connectivityManager.getNetworkCapabilities(network)
                        ?.let { activeNetwork ->
                            _hasInternet.value = when {
                                // Indicates this network uses a Wi-Fi transport,
                                // or WiFi has network connectivity
                                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                                    Pair(true, str)
                                // Indicates this network uses a Cellular transport. or
                                // Cellular has network connectivity
                                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                                    Pair(true, str)
                                else -> Pair(false, str)
                            }
                        }
                } ?: kotlin.run {
                    _hasInternet.value = Pair(false, str)
                }
            }?: run { _hasInternet.value = Pair(false, str) }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo?.let {
                val networkInfo =
                    connectivityManager.activeNetworkInfo
                @Suppress("DEPRECATION")
                _hasInternet.value = Pair(networkInfo?.isConnected!!, str)
            }?: run {
                _hasInternet.value = Pair(false, str)
            }
        }
    }
}
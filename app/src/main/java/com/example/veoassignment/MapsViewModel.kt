package com.example.veoassignment

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapsViewModel : ViewModel() {
    companion object {
        val defaultLocation = LatLng(-33.8523341, 151.2106085)
    }

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    var job: Job? = null

    var locationPermissionGranted = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    var lastKnownLocation: Location? = null
    var startLatLng: LatLng? = null
    var destinationLatLng: LatLng? = null

    var error = MutableLiveData<Error>()

    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        error.postValue(Error(throwable.localizedMessage))
    }


    fun drawRoute() {
        val retrofit = Retrofit
            .Builder()
            .baseUrl(MapsAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(MapsAPI::class.java)

        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = service.getDirection(
                BuildConfig.ROUTE_API_KEY,
                startLatLng.toLocation(),
                destinationLatLng.toLocation())

            Log.i("h", "response: ${response}")

            withContext(Dispatchers.Main){
                if (response.route != null){
                    Log.i("h", "route: ${response.route}")
                    response.route.legs?.get(0)?.let{
                        it.maneuvers
                    }
                }
            }
        }
    }
}

fun LatLng?.toLocation(): Location{
    val location = Location("")
    location.latitude = this?.latitude ?: MapsViewModel.defaultLocation.latitude
    location.longitude = this?.longitude ?: MapsViewModel.defaultLocation.longitude
    return location
}
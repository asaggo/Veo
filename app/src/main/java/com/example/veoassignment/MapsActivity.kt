package com.example.veoassignment

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.veoassignment.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private val TAG = MapsActivity::class.java.simpleName
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private val DEFAULT_ZOOM = 15

    lateinit var vm : MapsViewModel

    private var mMap: GoogleMap? = null
    private var startingPointMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private lateinit var binding: ActivityMapsBinding

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vm = ViewModelProvider(this).get(MapsViewModel::class.java)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onResume() {
        super.onResume()

        binding.btnCurrentLoc.setOnClickListener {
            getDeviceLocation()
        }

        binding.btnStart.setOnClickListener {
            vm.drawRoute()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

        mMap?.setOnMapClickListener {
            vm.destinationLatLng = it
            vm.destinationLatLng?.let { loc ->
                destinationMarker?.remove()
                destinationMarker =
                    addMarker(
                        loc,
                        "Destination",
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (mMap == null) return
        try {
            mMap?.isMyLocationEnabled = vm.locationPermissionGranted
            mMap?.uiSettings?.isMyLocationButtonEnabled = vm.locationPermissionGranted
            if (!vm.locationPermissionGranted) {
                vm.lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (vm.locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        vm.lastKnownLocation = task.result
                        vm.lastKnownLocation?.let {
                            vm.startLatLng = LatLng(it.latitude, it.longitude)
                            startingPointMarker = addMarker(vm.startLatLng!!, "Current Location")
//                            onMapUpdated(LatLng(it.latitude,it.longitude), "Current Location")
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(MapsViewModel.defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun addMarker(
        location: LatLng,
        title: String? = null,
        icon: BitmapDescriptor? = null
    ): Marker? {
        val markerOptions = MarkerOptions().position(location)
        title?.let {
            markerOptions.title(it)
        }
        icon?.let {
            markerOptions.icon(icon)
        }
        mMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                location, DEFAULT_ZOOM.toFloat()
            )
        )
        return mMap?.addMarker(markerOptions)
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */

        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            vm.locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        vm.locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                )
                    vm.locationPermissionGranted = true
            }
        }
        updateLocationUI()
    }


    override fun onLocationChanged(location: Location) {
        Toast.makeText(
            applicationContext,
            "Location Changed! ${location.latitude} , ${location.longitude}",
            Toast.LENGTH_LONG
        ).show()
    }
}


//https://www.wingsquare.com/blog/drawing-driving-route-directions-between-two-locations-using-google-directions-in-google-map-android/
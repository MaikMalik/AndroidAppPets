package com.dam2.app_pet2.ui.fragments.maps


import kotlinx.coroutines.*
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dam2.app_pet2.R
import com.dam2.app_pet2.databinding.FragmentMapsBinding
import com.dam2.app_pet2.network.models.maps.MyPlaces
import com.dam2.app_pet2.network.remote.Common
import com.dam2.app_pet2.network.remote.IGoogleAPIService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.type.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.maps.android.PolyUtil

class MapsFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var googleMap: GoogleMap

    lateinit var mService: IGoogleAPIService
    private val INITIAL_ZOOM_LEVEL = 15f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.MY_MAP) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize service
        mService = Common.googleApiService

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Update current location on the map
                    updateLocationOnMap(location)
                    // Get nearby places
                    getNearbyPlaces(location)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Check and request location permissions if needed
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.setOnMyLocationButtonClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation?.addOnSuccessListener { location ->
                    location?.let {
                        updateLocationOnMap(it)
                        getNearbyPlaces(it)
                    }
                }
            } else {
                requestLocationPermission()
            }
            true
        }
    }

    private fun updateLocationOnMap(location: Location) {
        val latLng = com.google.android.gms.maps.model.LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("Tu Posición")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

        googleMap.clear()
        googleMap.addMarker(markerOptions)
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(11f))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_ZOOM_LEVEL))
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.create().apply {
                interval = 5000
                fastestInterval = 3000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                smallestDisplacement = 10f
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, null
            )
        } else {
            requestLocationPermission()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    private fun getNearbyPlaces(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val radius = 1000 // Radius in meters
        val type = "veterinary_care"
        val apiKey = "AIzaSyBo0UOJ7YpQhXAodBcNVQQBDqEidpEeLYA"

        val url =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$latitude,$longitude&radius=$radius&type=$type&key=$apiKey"

        val call = mService.getNearbyPlaces(url)
        call.enqueue(object : Callback<MyPlaces> {
            override fun onResponse(call: Call<MyPlaces>, response: Response<MyPlaces>) {
                if (response.isSuccessful) {
                    val myPlaces = response.body()
                    if (myPlaces != null) {
                        val results = myPlaces.results
                        for (result in results) {
                            val latLng = com.google.android.gms.maps.model.LatLng(
                                result.geometry?.location?.lat ?: 0.0,
                                result.geometry?.location?.lng ?: 0.0
                            )
                            val markerOptions = MarkerOptions().position(latLng).title(result.name)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            googleMap.addMarker(markerOptions)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<MyPlaces>, t: Throwable) {
                // Handle errors
            }
        })
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

}
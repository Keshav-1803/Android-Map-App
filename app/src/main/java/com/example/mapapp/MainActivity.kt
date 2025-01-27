package com.example.mapapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.DirectionsResult
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.GeocodingResult
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var searchBar: EditText
    private lateinit var currentLocationButton: ImageButton
    private lateinit var routeButton: ImageButton
    private lateinit var segmentControl: RadioGroup
    private lateinit var placesClient: PlacesClient
    private lateinit var geoApiContext: GeoApiContext

    private var currentLocation: LatLng? = null
    private var selectedDestination: LatLng? = null

    // Permissions result launcher for location permissions
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            checkAndEnableLocation()
        } else {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Views
        searchBar = findViewById(R.id.searchBar)
        currentLocationButton = findViewById(R.id.currentLocationButton)
        routeButton = findViewById(R.id.routeButton)
        segmentControl = findViewById(R.id.segmentControl)

        // Initialize MapView and get the map asynchronously
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize GeoApiContext after onCreate
        geoApiContext = GeoApiContext.Builder()
            .apiKey(getString(R.string.API_KEY)) // Secure API Key from strings.xml
            .build()

        // Initialize Places Client
        initializePlaces()

        // Current Location Button Action
        currentLocationButton.setOnClickListener {
            if (::mMap.isInitialized && currentLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15f))
            } else {
                Toast.makeText(this, "Unable to retrieve current location.", Toast.LENGTH_SHORT).show()
            }
        }

        // Route Button Action
        routeButton.setOnClickListener {
            if (::mMap.isInitialized && currentLocation != null && selectedDestination != null) {
                showRoute(currentLocation!!, selectedDestination!!)
            } else {
                Toast.makeText(this, "Please select a destination first", Toast.LENGTH_SHORT).show()
            }
        }

        // Set Map Mode Based on Segment Control Selection
        segmentControl.setOnCheckedChangeListener { _, checkedId ->
            if (::mMap.isInitialized) {
                when (checkedId) {
                    R.id.mapMode -> mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    R.id.transportMode -> mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    R.id.hybridMode -> mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                }
            }
        }

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkAndEnableLocation()
        } else {
            // Request permission if not granted
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Set up the autocomplete feature for the search bar
        setUpSearchAutocomplete()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkAndEnableLocation() // Enable location once the map is ready
    }

    // Check and enable location
    private fun checkAndEnableLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Location is disabled, prompt the user to turn it on
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            // Enable location services if location is already enabled
            enableLocation()
        }
    }

    // Enable location settings
    private fun enableLocation() {
        if (::mMap.isInitialized) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap.isMyLocationEnabled = true
                getCurrentLocation()
            } else {
                locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            // Set Map Long Press Listener for Reverse Geocoding
            mMap.setOnMapLongClickListener { latLng ->
                reverseGeocode(latLng)
            }
        }
    }

    // Function to get current location
    private fun getCurrentLocation() {
        try {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                    if (::mMap.isInitialized) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15f))
                        mMap.addMarker(MarkerOptions().position(currentLocation!!).title("You are here"))
                    }
                } ?: run {
                    Toast.makeText(this, "Unable to retrieve current location.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission not granted: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Reverse Geocode on Map Long Click
    private fun reverseGeocode(latLng: LatLng) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geoApiLatLng = com.google.maps.model.LatLng(latLng.latitude, latLng.longitude)
                val results: Array<GeocodingResult> = GeocodingApi.reverseGeocode(geoApiContext, geoApiLatLng).await()

                withContext(Dispatchers.Main) {
                    if (results.isNotEmpty()) {
                        val address = results[0].formattedAddress ?: "Unknown location"
                        if (::mMap.isInitialized) {
                            mMap.addMarker(MarkerOptions().position(latLng).title(address))
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Unable to retrieve address", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error during geocoding: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Show Route between Current Location and Destination
    private fun showRoute(start: LatLng, end: LatLng) {
        if (::mMap.isInitialized) {
            val routeOptions = PolylineOptions()
                .add(start)
                .add(end)
                .width(10f)
                .color(ContextCompat.getColor(this, R.color.route_color))
            mMap.addPolyline(routeOptions)

            val bounds = LatLngBounds.Builder()
                .include(start)
                .include(end)
                .build()
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100)
            mMap.animateCamera(cameraUpdate)
        }
    }

    // Set up search autocomplete for place suggestions
    private fun setUpSearchAutocomplete() {
        val token = AutocompleteSessionToken.newInstance()
        searchBar.addTextChangedListener { editable ->
            if (editable?.isNotEmpty() == true) {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(editable.toString())
                    .setSessionToken(token)
                    .build()

                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        val predictions = response.autocompletePredictions
                        if (predictions.isNotEmpty()) {
                            // Handle the first prediction
                            val placeId = predictions[0].placeId
                            fetchPlaceDetails(placeId)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("AutocompleteError", "Error fetching suggestions", exception)
                        Toast.makeText(this, "Error fetching suggestions", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Fetch and set the selected place on the map
    private fun fetchPlaceDetails(placeId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, listOf(Place.Field.LAT_LNG)).build()
                val placeDetails = placesClient.fetchPlace(fetchPlaceRequest).await()

                val latLng = placeDetails.place.latLng
                latLng?.let {
                    // Call onPlaceSelected() when a place is selected
                    onPlaceSelected(it)
                }
            } catch (e: Exception) {
                Log.e("PlaceDetailsError", "Error fetching place details", e)
                Toast.makeText(this@MainActivity, "Error fetching place details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Set the selected destination for route calculation
    private fun onPlaceSelected(latLng: LatLng) {
        selectedDestination = latLng
        routeButton.isEnabled = true // Enable the route button once a destination is selected
    }

    // Initialize Places Client for autocomplete
    private fun initializePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.API_KEY))
        }
        placesClient = Places.createClient(this)
    }
}
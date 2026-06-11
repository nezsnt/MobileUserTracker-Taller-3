package com.example.taller3.view

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.taller3.R
import com.example.taller3.model.POI
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject

data class MapState(
    val currentLat: Double = 0.0,
    val currentLng: Double = 0.0,
    val pointsOfInterest: List<POI> = emptyList()
)

class MapViewModel : ViewModel() {
    private val _mapState = MutableStateFlow(MapState())
    val mapState = _mapState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    fun loadPOIsFromJson(context: Context) {
        if (_mapState.value.pointsOfInterest.isNotEmpty()) return

        try {
            val inputStream = context.resources.openRawResource(R.raw.locations)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("locationsArray")
            val list = mutableListOf<POI>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                list.add(
                    POI(
                        name = item.getString("name"),
                        latitude = item.getDouble("latitude"),
                        longitude = item.getDouble("longitude")
                    )
                )
            }
            _mapState.update { it.copy(pointsOfInterest = list) }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun updateUserLocation(lat: Double, lng: Double) {
        _mapState.update { it.copy(currentLat = lat, currentLng = lng) }

        val uid = auth.currentUser?.uid
        if (uid != null) {
            val userRef = database.getReference("users/$uid")
            val locationUpdates = mapOf<String, Any>(
                "latitude" to lat,
                "longitude" to lng
            )
            userRef.updateChildren(locationUpdates)
        }
    }
    fun createLocationRequest() : LocationRequest{
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(5000)
            .build()
        return locationRequest
    }
    fun createLocationCallback(onLocationChange : (LocationResult)-> Unit): LocationCallback {
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onLocationChange(locationResult)
            }
        }
        return callback
    }

}
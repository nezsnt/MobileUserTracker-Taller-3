package com.example.taller3.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller3.model.User
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.util.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

data class TrackingState(
    val myLocation: LatLng? = null,
    val targetUser: User? = null,
    val routePoints: List<LatLng> = emptyList(),
    val distanceInKm: Double = 0.0
)

class TrackingViewModel : ViewModel() {
    private val _trackingState = MutableStateFlow(TrackingState())
    val trackingState = _trackingState.asStateFlow()

    private val database = FirebaseDatabase.getInstance().getReference("users")
    private var targetUserListener: ValueEventListener? = null
    private lateinit var roadManager: OSRMRoadManager

    fun initialize(context: Context) {
        roadManager = OSRMRoadManager(context, "ANDROID")
    }

    fun startTracking(targetUserId: String) {
        if (targetUserListener != null) {
            onCleared()
        }
        targetUserListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                _trackingState.update { it.copy(targetUser = user) }
                calculateRouteAndDistance()
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child(targetUserId).addValueEventListener(targetUserListener!!)
    }

    fun updateMyPosition(lat: Double, lng: Double) {
        _trackingState.update { it.copy(myLocation = LatLng(lat, lng)) }
        calculateRouteAndDistance()
    }

    private fun calculateRouteAndDistance() {
        val state = _trackingState.value
        val myLoc = state.myLocation
        val targetLoc = state.targetUser?.let { LatLng(it.latitude, it.longitude) }

        if (myLoc != null && targetLoc != null && targetLoc.latitude != 0.0) {
            val distance = distance(myLoc.latitude, myLoc.longitude, targetLoc.latitude, targetLoc.longitude)
            _trackingState.update { it.copy(distanceInKm = distance) }
            //Hilo
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val startPoint = GeoPoint(myLoc.latitude, myLoc.longitude)
                    val endPoint = GeoPoint(targetLoc.latitude, targetLoc.longitude)
                    val waypoints = arrayListOf(startPoint, endPoint)

                    val road = roadManager.getRoad(waypoints)
                    if (road.mStatus == Road.STATUS_OK) {
                        val points = road.mRouteHigh.map { LatLng(it.latitude, it.longitude) }
                        _trackingState.update { it.copy(routePoints = points) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        targetUserListener?.let { listener ->
            _trackingState.value.targetUser?.uid?.let { uid ->
                database.child(uid).removeEventListener(listener)
            }
        }
    }

    private fun distance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val radiusOfEarthKm = 6371.0
        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lngDistance / 2) * sin(lngDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val result = radiusOfEarthKm * c
        return round(result * 100.0) / 100.0
    }
}
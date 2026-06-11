package com.example.taller3.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.taller3.view.MapViewModel
import com.example.taller3.R
import com.example.taller3.view.TrackingViewModel
import com.example.taller3.lightSensor
import com.example.taller3.navigation.AppScreens
import com.example.taller3.sensorManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTracker(navController: NavController, targetUserId: String, mapViewModel: MapViewModel = viewModel(), trackingViewModel: TrackingViewModel = viewModel()) {
    val context = LocalContext.current
    val state by trackingViewModel.trackingState.collectAsState()

    val lightMapStyle = MapStyleOptions.loadRawResourceStyle(context, R.raw.lightmap)
    val darkMapStyle = MapStyleOptions.loadRawResourceStyle(context, R.raw.darkmap)
    lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    var currentMapStyle by remember { mutableStateOf(lightMapStyle) }

    LaunchedEffect(Unit) {
        trackingViewModel.initialize(context)
        trackingViewModel.startTracking(targetUserId)
    }

    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationRequest = mapViewModel.createLocationRequest()
    val locationCallback = remember {
        mapViewModel.createLocationCallback { result ->
            result.lastLocation?.let { loc ->
                trackingViewModel.updateMyPosition(loc.latitude, loc.longitude)
                mapViewModel.updateUserLocation(loc.latitude, loc.longitude)
            }
        }
    }

    val sensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                val lux = event.values[0]
                currentMapStyle = if (lux < 2000) darkMapStyle else lightMapStyle
            }
        }
    }

    DisposableEffect(Unit) {
        lightSensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
        if (ContextCompat.checkSelfPermission(context, locationPermission) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
        onDispose { locationClient.removeLocationUpdates(locationCallback) }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.6097, -74.0817), 12f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.targetUser != null) "Tracking ${state.targetUser!!.name}" else "Loading...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(AppScreens.userList.name) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.RojoOscuro)
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val myMarkerState = rememberUpdatedMarkerState()
            val targetMarkerState = rememberUpdatedMarkerState()

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapStyleOptions = currentMapStyle)
            ) {
                state.myLocation?.let {
                    myMarkerState.position = it
                    Marker(
                        state = myMarkerState,
                        title = "My location",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }
                state.targetUser?.let { user ->
                    if(user.latitude != 0.0) {
                        targetMarkerState.position = LatLng(user.latitude, user.longitude)
                        Marker(state = targetMarkerState, title = user.name, snippet = "A ${state.distanceInKm} km")
                    }
                }
                if (state.routePoints.isNotEmpty()) {
                    Polyline(points = state.routePoints, color = colorResource(R.color.Rojo), width = 12f)
                }
            }

            if (state.targetUser != null && state.distanceInKm > 0.0) {
                ElevatedCard(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Distancia hacia ${state.targetUser!!.name}", fontSize = 16.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${state.distanceInKm} km", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.Rojo))
                    }
                }
            }
        }
    }
}
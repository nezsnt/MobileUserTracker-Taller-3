package com.example.taller3.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import com.example.taller3.R
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.taller3.view.AuthViewModel
import com.example.taller3.view.MapViewModel
import com.example.taller3.lightSensor
import com.example.taller3.navigation.AppScreens
import com.example.taller3.sensorManager
import com.example.taller3.util.ButtonShared
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.MapProperties

val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
val notificationPermission = Manifest.permission.POST_NOTIFICATIONS


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Home(controller: NavController, mapViewModel: MapViewModel = viewModel(), viewModel: AuthViewModel =viewModel()) {
    val permissionsToRequest = remember {
        val list = mutableListOf(locationPermission)
        list.add(notificationPermission)
        list
    }
    val multiplePermissionsState = rememberMultiplePermissionsState(permissionsToRequest)
    var showButton by remember { mutableStateOf(false) }

    SideEffect {
        if (!multiplePermissionsState.allPermissionsGranted) {
            if (multiplePermissionsState.shouldShowRationale) {
                showButton = true
            } else {
                showButton = false
                multiplePermissionsState.launchMultiplePermissionRequest()
            }
        }
    }

    if (multiplePermissionsState.allPermissionsGranted) {
        LocationWithRequest(controller, mapViewModel,viewModel)
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showButton) {
                Text("Access to GPS and Notifications are mandatory.")
                Spacer(modifier = Modifier.height(16.dp))
                ButtonShared("Request Permissions") {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                }
            } else {
                Text("No access to required permissions")
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState", "ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationWithRequest(navController: NavController, mapViewModel: MapViewModel, viewModel: AuthViewModel) {
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity

    BackHandler(enabled = true) {
        activity?.finish()
    }

    val state by mapViewModel.mapState.collectAsState()
    val auth by viewModel.authState.collectAsState()
    LaunchedEffect(Unit) {
        mapViewModel.loadPOIsFromJson(context)
        viewModel.fetchInitialStatus()
        viewModel.saveDeviceTokenAutomatically()
    }

    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationRequest = mapViewModel.createLocationRequest()

    val lightMapStyle = MapStyleOptions.loadRawResourceStyle(context, R.raw.lightmap)
    val darkMapStyle = MapStyleOptions.loadRawResourceStyle(context, R.raw.darkmap)
    lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    var currentMapStyle by remember { mutableStateOf(lightMapStyle) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(state.currentLat, state.currentLng), 14f)
    }

    val locationCallback = remember {
        mapViewModel.createLocationCallback { result ->
            result.lastLocation?.let { location ->
                mapViewModel.updateUserLocation(location.latitude, location.longitude)

                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(location.latitude, location.longitude), 14f
                )
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
            sensorManager.registerListener(
                sensorListener,
                lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        onDispose { sensorManager.unregisterListener(sensorListener) }

        if (ContextCompat.checkSelfPermission(
                context,
                locationPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
        onDispose {
            locationClient.removeLocationUpdates(locationCallback)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home", fontWeight = FontWeight.Bold) },
                actions = {
                    var expand by remember { mutableStateOf(false) }

                    IconButton(onClick = { expand = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }

                    DropdownMenu(
                        expanded = expand,
                        onDismissRequest = { expand = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("List of Users") },
                            onClick = {
                                expand = false
                                navController.navigate("userList")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                expand = false
                                viewModel.logOut()
                                navController.navigate(AppScreens.authentication.name) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.RojoOscuro),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapStyleOptions = currentMapStyle),
            ) {
                if (state.currentLat != 0.0 && state.currentLng != 0.0) {
                    Marker(
                        state = MarkerState(LatLng(state.currentLat, state.currentLng)),
                        title = "My Position",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }

                state.pointsOfInterest.forEach { poi ->
                    Marker(
                        state = MarkerState(LatLng(poi.latitude, poi.longitude)),
                        title = poi.name
                    )
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter).fillMaxWidth()
                    .padding(all = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ElevatedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.toggleAvailability()
                    }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = if (auth.available) colorResource(R.color.RojoPlano) else colorResource(R.color.Verde),
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = if (auth.available) "Not Available" else "Available",
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.black)
                        )
                    }

                }
            }
        }
    }
}
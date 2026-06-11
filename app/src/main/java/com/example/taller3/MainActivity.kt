package com.example.taller3

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.example.taller3.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var trackUserId = intent.getStringExtra("TRACK_USER_ID")
        if (trackUserId == null && intent.extras != null) {
            trackUserId = intent.extras?.getString("trackUserId")
        }
        val currentUser = FirebaseAuth.getInstance().currentUser
        setContent {
            Navigation(currentUser,trackUserId)
        }
    }
}

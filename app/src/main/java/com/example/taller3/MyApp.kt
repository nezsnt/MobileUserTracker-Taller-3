package com.example.taller3

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

lateinit var auth : FirebaseAuth
lateinit var database : FirebaseDatabase
var lightSensor : Sensor? = null
lateinit var sensorManager: SensorManager

class MyApp : Application() {
    companion object{
        const val NOTIFICATION_CHANNEL_ID =
            "notificaion_fcm"
    }
    override fun onCreate() {
        super.onCreate()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        createNotificationChannel()
    }
    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "FCM Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description="Channel for FCM Notifications"
            val notManager = getSystemService(NotificationManager::class.java)
            notManager.createNotificationChannel(channel)
        }
    }
}
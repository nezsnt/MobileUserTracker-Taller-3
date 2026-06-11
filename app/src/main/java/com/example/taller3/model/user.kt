package com.example.taller3.model

data class User(
    val uid: String = "",
    val name: String = "",
    val lastname: String = "",
    val id: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val available: Boolean = false
)
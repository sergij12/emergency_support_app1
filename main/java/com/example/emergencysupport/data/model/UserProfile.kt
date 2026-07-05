package com.example.emergencysupport.data.model

data class UserProfile(
    val fullName: String = "",
    val email: String = "",
    val city: String = "",
    val emergencyContact: String = "",
    val bloodType: String = "",
    val medicalNotes: String = "",
    val homeAddressHint: String = "",
    val isLoggedIn: Boolean = false
)

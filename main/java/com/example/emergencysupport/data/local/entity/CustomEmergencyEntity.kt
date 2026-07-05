package com.example.emergencysupport.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_emergencies")
data class CustomEmergencyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val planKey: String,
    val title: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

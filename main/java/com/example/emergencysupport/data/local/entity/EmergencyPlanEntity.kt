package com.example.emergencysupport.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_plans")
data class EmergencyPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val emergencyType: String,
    val stepOrder: Int,
    val stepText: String,
    val requiredResourceKey: String? = null
)

package com.example.emergencysupport.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val key: String,
    val title: String,
    val isAvailable: Boolean
)

package com.example.emergencysupport.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.emergencysupport.data.local.entity.CustomEmergencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomEmergencyDao {
    @Query("SELECT * FROM custom_emergencies ORDER BY createdAt DESC")
    fun getCustomEmergencies(): Flow<List<CustomEmergencyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomEmergency(item: CustomEmergencyEntity): Long

    @Query("DELETE FROM custom_emergencies WHERE id = :id")
    suspend fun deleteCustomEmergencyById(id: Int)
}

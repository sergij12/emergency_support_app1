package com.example.emergencysupport.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.emergencysupport.data.local.entity.EmergencyPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyPlanDao {
    @Query("SELECT * FROM emergency_plans WHERE emergencyType = :type ORDER BY stepOrder ASC")
    fun getPlanByType(type: String): Flow<List<EmergencyPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<EmergencyPlanEntity>)

    @Query("DELETE FROM emergency_plans WHERE emergencyType = :type")
    suspend fun deletePlanByType(type: String)

    @Query("SELECT COUNT(*) FROM emergency_plans")
    suspend fun countPlans(): Int
}

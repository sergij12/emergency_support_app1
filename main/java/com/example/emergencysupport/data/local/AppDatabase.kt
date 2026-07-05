package com.example.emergencysupport.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.emergencysupport.data.local.dao.ChecklistItemDao
import com.example.emergencysupport.data.local.dao.CustomEmergencyDao
import com.example.emergencysupport.data.local.dao.EmergencyPlanDao
import com.example.emergencysupport.data.local.dao.ResourceDao
import com.example.emergencysupport.data.local.entity.ChecklistItemEntity
import com.example.emergencysupport.data.local.entity.CustomEmergencyEntity
import com.example.emergencysupport.data.local.entity.EmergencyPlanEntity
import com.example.emergencysupport.data.local.entity.ResourceEntity

@Database(
    entities = [ResourceEntity::class, EmergencyPlanEntity::class, ChecklistItemEntity::class, CustomEmergencyEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resourceDao(): ResourceDao
    abstract fun emergencyPlanDao(): EmergencyPlanDao
    abstract fun checklistItemDao(): ChecklistItemDao
    abstract fun customEmergencyDao(): CustomEmergencyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "emergency_support_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

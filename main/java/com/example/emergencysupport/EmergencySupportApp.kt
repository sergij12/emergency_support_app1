package com.example.emergencysupport

import android.app.Application
import com.example.emergencysupport.data.local.AppDatabase
import com.example.emergencysupport.data.repository.EmergencyRepository
import com.example.emergencysupport.data.repository.SettingsRepository

class EmergencySupportApp : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: EmergencyRepository
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = EmergencyRepository(
            resourceDao = database.resourceDao(),
            planDao = database.emergencyPlanDao(),
            checklistItemDao = database.checklistItemDao(),
            customEmergencyDao = database.customEmergencyDao()
        )
        settingsRepository = SettingsRepository(this)
    }
}

package com.example.emergencysupport.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.emergencysupport.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val firstLaunchKey = booleanPreferencesKey("first_launch")
    private val loggedInKey = booleanPreferencesKey("logged_in")
    private val fullNameKey = stringPreferencesKey("full_name")
    private val emailKey = stringPreferencesKey("email")
    private val cityKey = stringPreferencesKey("city")
    private val emergencyContactKey = stringPreferencesKey("emergency_contact")
    private val bloodTypeKey = stringPreferencesKey("blood_type")
    private val medicalNotesKey = stringPreferencesKey("medical_notes")
    private val homeAddressHintKey = stringPreferencesKey("home_address_hint")

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[firstLaunchKey] ?: true
    }

    val userProfile: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            fullName = prefs[fullNameKey].orEmpty(),
            email = prefs[emailKey].orEmpty(),
            city = prefs[cityKey].orEmpty(),
            emergencyContact = prefs[emergencyContactKey].orEmpty(),
            bloodType = prefs[bloodTypeKey].orEmpty(),
            medicalNotes = prefs[medicalNotesKey].orEmpty(),
            homeAddressHint = prefs[homeAddressHintKey].orEmpty(),
            isLoggedIn = prefs[loggedInKey] ?: false
        )
    }

    suspend fun finishOnboarding() {
        context.dataStore.edit { prefs ->
            prefs[firstLaunchKey] = false
        }
    }

    suspend fun login(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[loggedInKey] = true
            prefs[fullNameKey] = profile.fullName
            prefs[emailKey] = profile.email
            prefs[cityKey] = profile.city
            prefs[emergencyContactKey] = profile.emergencyContact
            prefs[bloodTypeKey] = profile.bloodType
            prefs[medicalNotesKey] = profile.medicalNotes
            prefs[homeAddressHintKey] = profile.homeAddressHint
        }
    }

    suspend fun updateProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[fullNameKey] = profile.fullName
            prefs[emailKey] = profile.email
            prefs[cityKey] = profile.city
            prefs[emergencyContactKey] = profile.emergencyContact
            prefs[bloodTypeKey] = profile.bloodType
            prefs[medicalNotesKey] = profile.medicalNotes
            prefs[homeAddressHintKey] = profile.homeAddressHint
            prefs[loggedInKey] = profile.isLoggedIn
        }
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs[loggedInKey] = false
        }
    }
}

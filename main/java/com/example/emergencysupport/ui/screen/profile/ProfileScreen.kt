package com.example.emergencysupport.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emergencysupport.data.model.UserProfile
import com.example.emergencysupport.ui.theme.DeepOcean
import com.example.emergencysupport.ui.theme.MidnightNavy
import com.example.emergencysupport.ui.theme.RescueBlue
import com.example.emergencysupport.ui.viewmodel.MainViewModel

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val preparedness by viewModel.preparednessScore.collectAsStateWithLifecycle()
    val profileCompleteness by viewModel.profileCompleteness.collectAsStateWithLifecycle()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("") }
    var medicalNotes by remember { mutableStateOf("") }
    var homeAddressHint by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        fullName = profile.fullName
        email = profile.email
        city = profile.city
        emergencyContact = profile.emergencyContact
        bloodType = profile.bloodType
        medicalNotes = profile.medicalNotes
        homeAddressHint = profile.homeAddressHint
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(MidnightNavy, DeepOcean, RescueBlue)))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Профіль користувача", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                        Text(
                            text = if (profile.fullName.isBlank()) "Заповни профіль для персоналізації підказок" else profile.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text("Готовність профілю: $profileCompleteness%", color = MaterialTheme.colorScheme.onBackground)
                        Text("Загальна готовність набору: $preparedness%", color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(title = "Місто", value = if (profile.city.isBlank()) "—" else profile.city, modifier = Modifier.fillMaxWidth(0.5f))
                StatCard(title = "Кров", value = if (profile.bloodType.isBlank()) "—" else profile.bloodType, modifier = Modifier.fillMaxWidth())
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(title = "Контакт", value = if (profile.emergencyContact.isBlank()) "—" else profile.emergencyContact, modifier = Modifier.fillMaxWidth(0.5f))
                StatCard(title = "Статус", value = if (profile.isLoggedIn) "Активний" else "Гість", modifier = Modifier.fillMaxWidth())
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Редагування профілю", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Ім'я та прізвище") })
                    OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Email") })
                    OutlinedTextField(value = city, onValueChange = { city = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Місто") })
                    OutlinedTextField(value = emergencyContact, onValueChange = { emergencyContact = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Екстрений контакт") })
                    OutlinedTextField(value = bloodType, onValueChange = { bloodType = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Група крові") })
                    OutlinedTextField(value = medicalNotes, onValueChange = { medicalNotes = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Медичні нотатки") })
                    OutlinedTextField(value = homeAddressHint, onValueChange = { homeAddressHint = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Орієнтир адреси / місце збору") })
                    Button(
                        onClick = {
                            viewModel.saveProfile(
                                UserProfile(
                                    fullName = fullName,
                                    email = email,
                                    city = city,
                                    emergencyContact = emergencyContact,
                                    bloodType = bloodType,
                                    medicalNotes = medicalNotes,
                                    homeAddressHint = homeAddressHint,
                                    isLoggedIn = true
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Зберегти профіль")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

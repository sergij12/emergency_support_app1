package com.example.emergencysupport.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emergencysupport.data.model.EmergencyType
import com.example.emergencysupport.ui.component.SectionCard
import com.example.emergencysupport.ui.theme.CardDark
import com.example.emergencysupport.ui.theme.DeepOcean
import com.example.emergencysupport.ui.theme.EmergencyRed
import com.example.emergencysupport.ui.theme.MidnightNavy
import com.example.emergencysupport.ui.theme.RescueBlue
import com.example.emergencysupport.ui.theme.SafeMint
import com.example.emergencysupport.ui.theme.SafetyAmber
import com.example.emergencysupport.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenAlarm: () -> Unit,
    onOpenPlanner: () -> Unit,
    onOpenResources: () -> Unit,
    onOpenChecklist: () -> Unit,
    onOpenGuide: () -> Unit
) {
    val selectedType by viewModel.selectedEmergencyType.collectAsStateWithLifecycle()
    val selectedCustom by viewModel.selectedCustomEmergency.collectAsStateWithLifecycle()
    val customEmergencies by viewModel.customEmergencies.collectAsStateWithLifecycle()
    val preparednessScore by viewModel.preparednessScore.collectAsStateWithLifecycle()
    val smartAdvice by viewModel.smartAdvice.collectAsStateWithLifecycle()
    val availableResources by viewModel.availableResourcesCount.collectAsStateWithLifecycle()
    val totalResources by viewModel.totalResourcesCount.collectAsStateWithLifecycle()
    val checkedChecklistCount by viewModel.checkedChecklistCount.collectAsStateWithLifecycle()
    val totalChecklistCount by viewModel.totalChecklistCount.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val recommendedKit by viewModel.recommendedKit.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(MidnightNavy, DeepOcean, Color(0xFF631B2A))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatusBadge(text = "LIVE • Система моніторингу")
                        Text(
                            text = if (profile.fullName.isBlank()) {
                                "Emergency Support"
                            } else {
                                "${profile.fullName}, система готова"
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Інтелектуальна підтримка для пожежі, витоку газу, евакуації, повені, медичних випадків та інших кризових подій.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SmallHeroChip(text = "Сценарій ${selectedCustom?.title ?: selectedType.title}")
                            SmallHeroChip(text = "Готовність $preparednessScore%")
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardMetricCard(
                    title = "Ресурси",
                    value = "$availableResources/$totalResources",
                    accent = RescueBlue,
                    modifier = Modifier.width(112.dp)
                )
                DashboardMetricCard(
                    title = "Чек-лист",
                    value = "$checkedChecklistCount/$totalChecklistCount",
                    accent = SafeMint,
                    modifier = Modifier.width(112.dp)
                )
                DashboardMetricCard(
                    title = "Режим",
                    value = "у роботі",
                    accent = SafetyAmber,
                    modifier = Modifier.width(112.dp)
                )
            }
        }


        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181D31))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Діаграми готовності",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    DiagramRow("Загальна готовність", preparednessScore / 100f, "$preparednessScore%")
                    DiagramRow("Ресурси", if (totalResources == 0) 0f else availableResources.toFloat() / totalResources, "$availableResources/$totalResources")
                    DiagramRow("Чек-лист", if (totalChecklistCount == 0) 0f else checkedChecklistCount.toFloat() / totalChecklistCount, "$checkedChecklistCount/$totalChecklistCount")
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile(
                    title = "Швидкі дії",
                    icon = Icons.Default.Warning,
                    accent = EmergencyRed,
                    onClick = onOpenAlarm,
                    modifier = Modifier.width(112.dp)
                )
                ActionTile(
                    title = "Ресурси",
                    icon = Icons.Default.Map,
                    accent = RescueBlue,
                    onClick = onOpenResources,
                    modifier = Modifier.width(112.dp)
                )
                ActionTile(
                    title = "План",
                    icon = Icons.Default.Shield,
                    accent = SafeMint,
                    onClick = onOpenPlanner,
                    modifier = Modifier.width(112.dp)
                )
            }
        }

        item {
            Text(
                text = "Сценарії надзвичайних ситуацій",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EmergencyType.entries.forEach { type ->
                    val active = selectedCustom == null && type == selectedType
                    Card(
                        modifier = Modifier
                            .width(188.dp)
                            .clickable { viewModel.selectEmergencyType(type) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (active) Color(0xFF2B1B4A) else CardDark
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = type.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = type.shortDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f)
                            )
                        }
                    }
                }


                customEmergencies.forEach { scenario ->
                    val active = selectedCustom?.id == scenario.id
                    Card(
                        modifier = Modifier
                            .width(210.dp)
                            .clickable {
                                viewModel.selectCustomEmergency(scenario)
                                onOpenPlanner()
                            },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (active) Color(0xFF123A34) else Color(0xFF182235)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI • ${scenario.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.deleteCustomEmergency(scenario) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Видалити НС",
                                        tint = EmergencyRed
                                    )
                                }
                            }
                            Text(
                                text = "Персональні підказки",
                                style = MaterialTheme.typography.bodySmall,
                                color = SafetyAmber,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = scenario.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF132C2C))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Персональні підказки",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (smartAdvice.isEmpty()) {
                        Text("Поки що підказок немає. Обери сценарій або заповни профіль.")
                    } else {
                        smartAdvice.forEach { tip ->
                            Text("• $tip", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Що ще покласти у набір",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (recommendedKit.isEmpty()) {
                        Text("Базовий набір уже зібрано добре. Перевір маршрути та екстрені контакти.")
                    } else {
                        recommendedKit.forEach { Text("• $it", color = MaterialTheme.colorScheme.onSurface) }
                    }
                }
            }
        }

        if (profile.bloodType.isNotBlank() || profile.emergencyContact.isNotBlank() || profile.medicalNotes.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2138))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Особиста картка безпеки",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (profile.bloodType.isNotBlank()) Text("Група крові: ${profile.bloodType}")
                        if (profile.emergencyContact.isNotBlank()) Text("Екстрений контакт: ${profile.emergencyContact}")
                        if (profile.medicalNotes.isNotBlank()) Text("Медичні нотатки: ${profile.medicalNotes}")
                    }
                }
            }
        }

        item {
            SectionCard(
                title = "Персональний план дій",
                subtitle = "Покроковий сценарій для вибраної надзвичайної ситуації.",
                onClick = onOpenPlanner
            )
        }
        item {
            SectionCard(
                title = "Чек-лист готовності",
                subtitle = "Контроль виконання важливих дій перед кризовою ситуацією.",
                onClick = onOpenChecklist
            )
        }
        item {
            SectionCard(
                title = "Розширений довідник",
                subtitle = "Короткі інструкції для пожежі, витоку газу, повені, медичних випадків і відключень.",
                onClick = onOpenGuide
            )
        }
        item { Box(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun StatusBadge(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0x33FFFFFF), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SmallHeroChip(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0x22FFFFFF), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(accent.copy(alpha = 0.18f), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(title, style = MaterialTheme.typography.labelMedium, color = accent)
            }
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(accent.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Icon(icon, contentDescription = title, tint = accent)
            }
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        }
    }
}


@Composable
private fun DiagramRow(title: String, progress: Float, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = MaterialTheme.colorScheme.onSurface)
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
    }
}

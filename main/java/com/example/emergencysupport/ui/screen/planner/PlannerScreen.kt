package com.example.emergencysupport.ui.screen.planner

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emergencysupport.data.model.EmergencyType
import com.example.emergencysupport.ui.component.PlanStepCard
import com.example.emergencysupport.ui.theme.CardDark
import com.example.emergencysupport.ui.theme.EmergencyRed
import com.example.emergencysupport.ui.theme.RescueBlue
import com.example.emergencysupport.ui.theme.SafeMint
import com.example.emergencysupport.ui.theme.SafetyAmber
import com.example.emergencysupport.ui.viewmodel.MainViewModel

@Composable
fun PlannerScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val selectedType by viewModel.selectedEmergencyType.collectAsStateWithLifecycle()
    val selectedCustom by viewModel.selectedCustomEmergency.collectAsStateWithLifecycle()
    val customEmergencies by viewModel.customEmergencies.collectAsStateWithLifecycle()
    val resources by viewModel.resources.collectAsStateWithLifecycle()
    val smartAdvice by viewModel.smartAdvice.collectAsStateWithLifecycle()
    val missingResources by viewModel.missingCriticalResources.collectAsStateWithLifecycle()
    val isCustomPlanLoading by viewModel.isCustomPlanLoading.collectAsStateWithLifecycle()
    val customPlanMessage by viewModel.customPlanMessage.collectAsStateWithLifecycle()

    val planItems by if (selectedCustom != null) {
        viewModel.getPlanFlowByKey(selectedCustom!!.planKey).collectAsStateWithLifecycle(initialValue = emptyList())
    } else {
        viewModel.getPlanFlow(selectedType).collectAsStateWithLifecycle(initialValue = emptyList())
    }

    var customTitle by remember { mutableStateOf("") }
    var customDescription by remember { mutableStateOf("") }

    fun openDialer(number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        context.startActivity(intent)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Персональний план дій",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF132C2C))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Додати власну НС через AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Введи назву надзвичайної ситуації, а Gemini AI сформує правильні кроки та додасть сценарій у План і на Головну.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Назва НС") },
                        placeholder = { Text("Наприклад: загубилась дитина, обвал будинку, землетрус") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customDescription,
                        onValueChange = { customDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Опис або деталі") },
                        placeholder = { Text("Де сталося, які ризики, хто поруч...") },
                        minLines = 2,
                        maxLines = 4
                    )

                    Button(
                        onClick = {
                            viewModel.createCustomEmergency(customTitle, customDescription)
                            customTitle = ""
                            customDescription = ""
                        },
                        enabled = customTitle.isNotBlank() && !isCustomPlanLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Згенерувати та додати НС")
                    }

                    if (isCustomPlanLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("AI створює план...")
                        }
                    }

                    customPlanMessage?.let {
                        Text(text = it, color = SafetyAmber)
                    }
                }
            }
        }

        item {
            Text(
                text = "Обери сценарій",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmergencyType.entries.forEach { type ->
                    val active = selectedCustom == null && type == selectedType
                    Card(
                        modifier = Modifier
                            .width(180.dp)
                            .clickable { viewModel.selectEmergencyType(type) },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (active) Color(0xFF2B1B4A) else CardDark
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(type.title, fontWeight = FontWeight.Bold)
                            Text(type.shortDescription, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                customEmergencies.forEach { scenario ->
                    val active = selectedCustom?.id == scenario.id
                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            .clickable { viewModel.selectCustomEmergency(scenario) },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (active) Color(0xFF123A34) else Color(0xFF182235)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI • ${scenario.title}",
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
                            Text(scenario.description, style = MaterialTheme.typography.bodySmall)
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Екстрені контакти",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickDialCard("101", "Пожежна", EmergencyRed, Icons.Default.Warning, Modifier.width(102.dp)) { openDialer("101") }
                        QuickDialCard("102", "Поліція", RescueBlue, Icons.Default.LocalPolice, Modifier.width(102.dp)) { openDialer("102") }
                        QuickDialCard("103", "Швидка", SafeMint, Icons.Default.LocalTaxi, Modifier.width(102.dp)) { openDialer("103") }
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Тактичні підказки", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    smartAdvice.forEach { advice ->
                        Text("• $advice")
                    }
                    if (missingResources.isNotEmpty()) {
                        Text(
                            text = "Ще варто підготувати: ${missingResources.joinToString()}",
                            color = SafetyAmber
                        )
                    }
                }
            }
        }

        if (planItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Для цього сценарію поки немає кроків. Обери інший тип НС або додай власний сценарій.")
                    }
                }
            }
        } else {
            items(planItems, key = { it.id }) { item ->
                val highlighted = item.requiredResourceKey?.let { key ->
                    resources.any { it.key == key && it.isAvailable }
                } ?: false

                PlanStepCard(
                    stepNumber = item.stepOrder,
                    text = item.stepText,
                    highlighted = highlighted,
                    visual = visualForPlanStep(item.stepText)
                )
            }
        }
    }
}

@Composable
private fun QuickDialCard(
    number: String,
    label: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(accent.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                    .padding(8.dp)
            ) {
                Icon(icon, contentDescription = label, tint = accent)
            }
            Text(number, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun visualForPlanStep(text: String): String = when {
    text.contains("документ", ignoreCase = true) -> "📄"
    text.contains("аптеч", ignoreCase = true) || text.contains("ліки", ignoreCase = true) -> "🧰"
    text.contains("воду", ignoreCase = true) || text.contains("води", ignoreCase = true) -> "💧"
    text.contains("ліхтар", ignoreCase = true) || text.contains("світ", ignoreCase = true) -> "🔦"
    text.contains("телефон", ignoreCase = true) || text.contains("павербанк", ignoreCase = true) -> "🔋"
    text.contains("укрит", ignoreCase = true) || text.contains("евак", ignoreCase = true) -> "🏃"
    text.contains("газ", ignoreCase = true) -> "⚠️"
    text.contains("вікна", ignoreCase = true) || text.contains("двері", ignoreCase = true) -> "🏠"
    text.contains("радіо", ignoreCase = true) -> "📻"
    else -> "🛡️"
}

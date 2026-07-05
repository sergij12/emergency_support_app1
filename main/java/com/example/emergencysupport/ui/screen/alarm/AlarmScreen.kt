package com.example.emergencysupport.ui.screen.alarm

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emergencysupport.ui.theme.CardDark
import com.example.emergencysupport.ui.theme.EmergencyRed
import com.example.emergencysupport.ui.theme.RescueBlue
import com.example.emergencysupport.ui.theme.SafeMint
import com.example.emergencysupport.ui.viewmodel.MainViewModel

@Composable
fun AlarmScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val selectedType by viewModel.selectedEmergencyType.collectAsStateWithLifecycle()
    val planItems by viewModel.getPlanFlow(selectedType).collectAsStateWithLifecycle(initialValue = emptyList())
    val missingResources by viewModel.missingCriticalResources.collectAsStateWithLifecycle()
    val recommendedKit by viewModel.recommendedKit.collectAsStateWithLifecycle()

    fun openDialer(number: String) {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
    }

    fun openMap(query: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}"))
        context.startActivity(intent)
    }

    fun shareEmergencyNote() {
        val text = buildString {
            append("Надзвичайна ситуація: ${selectedType.title}. ")
            append("Перші кроки: ")
            append(planItems.take(3).joinToString("; ") { "${it.stepOrder}. ${it.stepText}" })
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Поділитися планом"))
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
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Центр швидких дій", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Активний сценарій: ${selectedType.title}", color = MaterialTheme.colorScheme.primary)
                    Text(selectedType.shortDescription)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { openDialer("112") },
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = EmergencyRed)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("SOS ЕКСТРЕНА ДОПОМОГА", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Натисни, щоб відкрити виклик 112", color = Color.White)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                QuickActionCard("101", "Пожежна", EmergencyRed, Icons.Default.Warning, Modifier.width(102.dp)) { openDialer("101") }
                QuickActionCard("102", "Поліція", RescueBlue, Icons.Default.LocalPolice, Modifier.width(102.dp)) { openDialer("102") }
                QuickActionCard("103", "Швидка", SafeMint, Icons.Default.LocalHospital, Modifier.width(102.dp)) { openDialer("103") }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SmallUtilityCard("Лікарні поруч", Icons.Default.LocationOn, Modifier.width(156.dp)) { openMap("лікарня поруч") }
                SmallUtilityCard("Укриття поруч", Icons.Default.LocationOn, Modifier.width(156.dp)) { openMap("укриття поруч") }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SmallUtilityCard("Пункти незламності", Icons.Default.LocationOn, Modifier.width(156.dp)) { openMap("пункт незламності поруч") }
                SmallUtilityCard("Поділитися", Icons.Default.Share, Modifier.width(156.dp)) { shareEmergencyNote() }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF181D31))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Перші кроки", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    planItems.take(5).forEach { item -> Text("${item.stepOrder}. ${item.stepText}") }
                }
            }
        }

        if (missingResources.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF2F2212))) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Ще варто підготувати", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        missingResources.forEach { Text("• $it") }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF132C2C))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Рекомендований набір саме зараз", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    recommendedKit.take(6).forEach { Text("• $it") }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    value: String,
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
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.Start) {
            Box(
                modifier = Modifier
                    .background(accent.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                    .padding(8.dp)
            ) {
                Icon(icon, contentDescription = value, tint = accent)
            }
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SmallUtilityCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Text(title, fontWeight = FontWeight.SemiBold)
        }
    }
}

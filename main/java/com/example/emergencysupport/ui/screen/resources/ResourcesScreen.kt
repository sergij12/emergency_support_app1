package com.example.emergencysupport.ui.screen.resources

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Shield
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
import com.example.emergencysupport.ui.component.ResourceToggleCard
import com.example.emergencysupport.ui.viewmodel.MainViewModel

@Composable
fun ResourcesScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val resources by viewModel.resources.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedEmergencyType.collectAsStateWithLifecycle()
    val missingCritical by viewModel.missingCriticalResources.collectAsStateWithLifecycle()

    fun openMap(query: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}")))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Мої ресурси та карти", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Сценарій: ${selectedType.title}")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MapCard("Лікарні", "Знайти найближчу допомогу", "🏥", Icons.Default.LocalHospital, Modifier.width(190.dp)) { openMap("лікарня поруч") }
            MapCard("Укриття", "Сховища та безпечні місця", "🛡️", Icons.Default.Shield, Modifier.width(190.dp)) { openMap("укриття поруч") }
            MapCard("Пункти незламності", "Тепло, зарядка, зв'язок", "⚡", Icons.Default.Power, Modifier.width(210.dp)) { openMap("пункт незламності поруч") }
            MapCard("Аптеки", "Ліки та базова допомога", "💊", Icons.Default.LocationOn, Modifier.width(190.dp)) { openMap("аптека поруч") }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF18233A))) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Корисні локації", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("У розділ додано окремі карти: лікарні, укриття, пункти незламності та аптеки. Натискання відкриває пошук у Google Maps.")
            }
        }

        if (missingCritical.isNotEmpty()) {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Критично для цього сценарію", style = MaterialTheme.typography.titleMedium)
                    missingCritical.forEach { Text("• $it") }
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(resources, key = { it.id }) { item ->
                ResourceToggleCard(
                    title = item.title,
                    checked = item.isAvailable,
                    onCheckedChange = { viewModel.toggleResource(item) }
                )
            }
        }
    }
}

@Composable
private fun MapCard(
    title: String,
    subtitle: String,
    visual: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151B2F))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFF24385E), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(visual, style = MaterialTheme.typography.headlineSmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

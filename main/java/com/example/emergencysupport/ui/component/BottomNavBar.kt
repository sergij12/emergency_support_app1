package com.example.emergencysupport.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.emergencysupport.ui.navigation.Screen

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        Triple(Screen.Home.route, "Головна", Icons.Default.Home),
        Triple(Screen.Planner.route, "План", Icons.Default.Warning),
        Triple(Screen.Checklist.route, "Чек-лист", Icons.Default.Checklist),
        Triple(Screen.AiAssistant.route, "AI", Icons.Default.Psychology),
        Triple(Screen.Profile.route, "Профіль", Icons.Default.Person)
    )

    NavigationBar {
        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}

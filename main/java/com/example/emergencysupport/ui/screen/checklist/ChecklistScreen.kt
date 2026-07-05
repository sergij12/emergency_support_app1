package com.example.emergencysupport.ui.screen.checklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emergencysupport.data.local.entity.ChecklistItemEntity
import com.example.emergencysupport.ui.viewmodel.MainViewModel

@Composable
fun ChecklistScreen(viewModel: MainViewModel) {
    val checklist by viewModel.checklist.collectAsStateWithLifecycle()
    val checkedCount by viewModel.checkedChecklistCount.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalChecklistCount.collectAsStateWithLifecycle()
    var editingItem by remember { mutableStateOf<ChecklistItemEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    val progress = if (totalCount == 0) 0f else checkedCount.toFloat() / totalCount

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Чек-лист готовності", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Прогрес підготовки", style = MaterialTheme.typography.labelLarge)
                    Text("$checkedCount із $totalCount пунктів виконано", style = MaterialTheme.typography.titleLarge)
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    Text("Тепер список можна змінювати: додавати, редагувати і видаляти власні пункти.")
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(checklist, key = { it.id }) { item ->
                    Card(shape = RoundedCornerShape(22.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF23395D), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(iconForCategory(item.category))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(text = item.category, style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { editingItem = item }) {
                                Icon(Icons.Default.Edit, contentDescription = "Редагувати")
                            }
                            IconButton(onClick = { viewModel.deleteChecklistItem(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Видалити")
                            }
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { viewModel.toggleChecklistItem(item) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Додати пункт")
        }
    }

    if (showAddDialog) {
        ChecklistEditorDialog(
            title = "Новий пункт",
            initialTitle = "",
            initialCategory = "Власне",
            onDismiss = { showAddDialog = false },
            onSave = { itemTitle, category ->
                viewModel.addChecklistItem(itemTitle, category)
                showAddDialog = false
            }
        )
    }

    editingItem?.let { item ->
        ChecklistEditorDialog(
            title = "Редагувати пункт",
            initialTitle = item.title,
            initialCategory = item.category,
            onDismiss = { editingItem = null },
            onSave = { itemTitle, category ->
                viewModel.editChecklistItem(item, itemTitle, category)
                editingItem = null
            }
        )
    }
}

@Composable
private fun ChecklistEditorDialog(
    title: String,
    initialTitle: String,
    initialCategory: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var itemTitle by remember { mutableStateOf(initialTitle) }
    var category by remember { mutableStateOf(initialCategory) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = itemTitle, onValueChange = { itemTitle = it }, label = { Text("Назва пункту") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Категорія") })
            }
        },
        confirmButton = { Button(onClick = { onSave(itemTitle, category) }) { Text("Зберегти") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } }
    )
}

private fun iconForCategory(category: String): String = when {
    category.contains("мед", ignoreCase = true) || category.contains("апт", ignoreCase = true) -> "🧰"
    category.contains("док", ignoreCase = true) || category.contains("зв", ignoreCase = true) -> "📄"
    category.contains("дім", ignoreCase = true) -> "🏠"
    category.contains("евак", ignoreCase = true) -> "🎒"
    else -> "✅"
}

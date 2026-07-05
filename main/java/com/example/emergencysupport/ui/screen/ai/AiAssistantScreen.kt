package com.example.emergencysupport.ui.screen.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emergencysupport.data.model.AiChatMessage
import com.example.emergencysupport.data.model.EmergencyType
import com.example.emergencysupport.ui.theme.CardDark
import com.example.emergencysupport.ui.theme.DeepOcean
import com.example.emergencysupport.ui.theme.MidnightNavy
import com.example.emergencysupport.ui.theme.RescueBlue
import com.example.emergencysupport.ui.theme.SafeMint
import com.example.emergencysupport.ui.theme.SafetyAmber
import com.example.emergencysupport.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(viewModel: MainViewModel) {
    val messages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedEmergencyType.collectAsStateWithLifecycle()
    val preparednessScore by viewModel.preparednessScore.collectAsStateWithLifecycle()
    val profileCompleteness by viewModel.profileCompleteness.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    var question by rememberSaveable { mutableStateOf("") }

    fun sendQuestion(text: String = question) {
        if (text.isNotBlank()) {
            viewModel.askAi(text)
            question = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DeepOcean)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = SafetyAmber)
                    Text(
                        text = "AI-помічник з НС",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Працює як справжній онлайн-AI через Google Gemini API: відповідає на будь-які питання та враховує сценарій НС, профіль, ресурси й чек-лист. Якщо ключ не задано, автоматично вмикається резервний локальний режим.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.86f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusPill("Сценарій: ${selectedType.title}", RescueBlue)
                    StatusPill("Готовність: $preparednessScore%", SafeMint)
                    StatusPill("Профіль: $profileCompleteness%", SafetyAmber)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EmergencyType.entries.take(5).forEach { type ->
                SuggestionChip(
                    onClick = { viewModel.selectEmergencyType(type) },
                    label = { Text(type.title, maxLines = 1) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (type == selectedType) Color(0xFF2B1B4A) else CardDark,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickQuestionChip("Що робити зараз?") { sendQuestion("Що робити зараз при сценарії ${selectedType.title}?") }
            QuickQuestionChip("Що підготувати?") { sendQuestion("Що потрібно підготувати для сценарію ${selectedType.title}?") }
            QuickQuestionChip("Оціни ризик") { sendQuestion("Оціни мій рівень готовності та ризики") }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
            if (isAiLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = CardDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator()
                                Text("AI формує відповідь...", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Запитай AI: пожежа, газ, евакуація...") },
                minLines = 1,
                maxLines = 3,
                shape = RoundedCornerShape(22.dp)
            )
            IconButton(onClick = { viewModel.clearAiChat() }) {
                Icon(Icons.Default.Delete, contentDescription = "Очистити чат")
            }
            Button(onClick = { sendQuestion() }, enabled = !isAiLoading, shape = RoundedCornerShape(20.dp)) {
                Icon(Icons.Default.Send, contentDescription = "Надіслати")
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, accent: Color) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.18f))
    ) {
        Text(
            text = text,
            modifier = Modifier
                .widthIn(min = 96.dp)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun QuickQuestionChip(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ChatBubble(message: AiChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(if (message.isUser) 0.82f else 0.94f),
            shape = RoundedCornerShape(
                topStart = 22.dp,
                topEnd = 22.dp,
                bottomStart = if (message.isUser) 22.dp else 6.dp,
                bottomEnd = if (message.isUser) 6.dp else 22.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) RescueBlue.copy(alpha = 0.28f) else CardDark
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(14.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

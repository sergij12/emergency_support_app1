package com.example.emergencysupport.ui.screen.guide

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class GuideItem(
    val icon: String,
    val title: String,
    val category: String,
    val risk: String,
    val firstAction: String,
    val details: String
)

@Composable
fun GuideScreen() {
    var query by remember { mutableStateOf("") }
    val items = remember {
        listOf(
            GuideItem("🔥", "Пожежа", "Евакуація", "Високий ризик", "Вийти без ліфта", "Тримайтеся нижче диму, закрийте двері за собою, після виходу телефонуйте 101."),
            GuideItem("🔦", "Відключення світла", "Автономність", "Середній ризик", "Увімкнути резервне світло", "Економте заряд телефону, використовуйте павербанк, ліхтарик, радіо та запас води."),
            GuideItem("🚨", "Повітряна тривога", "Укриття", "Високий ризик", "Перейти в укриття", "Візьміть документи, воду, аптечку та залишайтеся в укритті до офіційного відбою."),
            GuideItem("💧", "Відсутність води", "Побут", "Середній ризик", "Перевірити запас", "Розділіть питну й технічну воду, підготуйте чисті ємності та засоби гігієни."),
            GuideItem("🧰", "Перша допомога", "Медицина", "Високий ризик", "Оцінити стан", "Усуньте небезпеку, перевірте дихання, використайте аптечку та викликайте 103 за потреби."),
            GuideItem("🌊", "Повінь", "Евакуація", "Високий ризик", "Підняти документи", "Вимкніть електрику, підніміть речі вище, підготуйте маршрут евакуації."),
            GuideItem("☣️", "Хімічна небезпека", "Захист", "Високий ризик", "Ізолювати приміщення", "Зачиніть вікна й двері, використайте респіратор або вологу тканину, слухайте офіційні повідомлення."),
            GuideItem("⚠️", "Витік газу", "Дім", "Критичний ризик", "Не вмикати світло", "Перекрийте газ, відкрийте вікна, вийдіть із приміщення та телефонуйте 104."),
            GuideItem("❄️", "Мороз / заметіль", "Погода", "Середній ризик", "Зберегти тепло", "Перевірте теплий одяг, пледи, заряд пристроїв, запас їжі та мінімізуйте вихід надвір."),
            GuideItem("🎒", "Термінова евакуація", "Маршрут", "Високий ризик", "Взяти тривожну валізу", "Документи, вода, аптечка, телефон, зарядка, готівка й заздалегідь визначене місце збору.")
        )
    }

    val filteredItems = items.filter {
        query.isBlank() || it.title.contains(query, true) || it.category.contains(query, true) || it.details.contains(query, true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Довідник дій", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Не просто текст: кожна картка має позначку, рівень ризику й першу дію.")
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Пошук за ситуацією") }
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181D31))
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .background(Color(0xFF2C3F67), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text(item.icon, style = MaterialTheme.typography.headlineSmall) }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Badge(item.category)
                                Badge(item.risk)
                            }
                            Text("Перша дія: ${item.firstAction}", fontWeight = FontWeight.SemiBold)
                            Text(item.details)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Badge(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0x332196F3), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}

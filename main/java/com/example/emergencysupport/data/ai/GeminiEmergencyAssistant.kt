package com.example.emergencysupport.data.ai

import com.example.emergencysupport.data.local.entity.ChecklistItemEntity
import com.example.emergencysupport.data.local.entity.ResourceEntity
import com.example.emergencysupport.data.model.EmergencyType
import com.example.emergencysupport.data.model.GeneratedEmergencyPlan
import com.example.emergencysupport.data.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import android.util.Log

class GeminiEmergencyAssistant(
    private val apiKey: String,
    private val model: String
) {
    suspend fun ask(
        question: String,
        emergencyType: EmergencyType,
        resources: List<ResourceEntity>,
        checklist: List<ChecklistItemEntity>,
        profile: UserProfile,
        preparednessScore: Int,
        previousMessages: List<Pair<Boolean, String>>
    ): Result<String> = runCatching {
        require(apiKey.isNotBlank()) { "Gemini API key is empty" }

        val encodedKey = URLEncoder.encode(apiKey, Charsets.UTF_8.name())
        val preferredModel = model.trim().ifBlank { "gemini-2.5-flash" }

        val contents = JSONArray().apply {
            put(content("user", buildSystemPrompt() + "\n\n" + buildContextPrompt(emergencyType, resources, checklist, profile, preparednessScore)))
            previousMessages.takeLast(8).forEach { (isUser, text) ->
                put(content(if (isUser) "user" else "model", text))
            }
            put(content("user", question))
        }

        val body = JSONObject()
            .put("contents", contents)
            .put(
                "generationConfig",
                JSONObject()
                    .put("temperature", 0.35)
                    .put("maxOutputTokens", 900)
            )
            .toString()

        Log.d("GeminiTest", "Запит користувача: $question")
        Log.d("GeminiTest", "Request body: $body")

        requestWithFallbackModels(encodedKey, preferredModel, body)
    }

    suspend fun generateCustomEmergencyPlan(
        title: String,
        description: String,
        resources: List<ResourceEntity>,
        checklist: List<ChecklistItemEntity>,
        profile: UserProfile,
        preparednessScore: Int
    ): Result<GeneratedEmergencyPlan> = runCatching {
        require(apiKey.isNotBlank()) { "Gemini API key is empty" }

        val cleanTitle = title.trim().ifBlank { "Власна надзвичайна ситуація" }
        val cleanDescription = description.trim()
        val encodedKey = URLEncoder.encode(apiKey, Charsets.UTF_8.name())
        val preferredModel = model.trim().ifBlank { "gemini-2.5-flash" }

        val readyResources = resources.filter { it.isAvailable }.joinToString { it.title }.ifBlank { "немає позначених" }
        val missingResources = resources.filterNot { it.isAvailable }.take(12).joinToString { it.title }.ifBlank { "немає" }
        val uncheckedChecklist = checklist.filterNot { it.isChecked }.take(12).joinToString { it.title }.ifBlank { "немає" }
        val profileCity = profile.city.ifBlank { "не вказано" }
        val medicalNotes = profile.medicalNotes.ifBlank { "не вказано" }

        val prompt = """
            Ти — експерт з цивільного захисту України та помічник мобільного застосунку EmergencySupportApp.

            Користувач створює ВЛАСНУ надзвичайну ситуацію:
            Назва: "$cleanTitle"
            Опис/деталі: "${cleanDescription.ifBlank { "деталі не вказані" }}"

            Контекст користувача:
            - місто: $profileCity
            - медичні нотатки: $medicalNotes
            - рівень готовності: $preparednessScore%
            - доступні ресурси: $readyResources
            - відсутні ресурси: $missingResources
            - невиконані пункти чек-листа: $uncheckedChecklist

            Сформуй правильний план дій саме для цієї ситуації.

            Вимоги до кроків:
            1. Кроки мають бути конкретні саме для ситуації "$cleanTitle", а не універсальні.
            2. Не починай кожен пункт словами "зберігайте спокій" або "оцініть ситуацію".
            3. Спочатку захист життя, потім виклик служб, потім ресурси/документи/дії після небезпеки.
            4. Якщо доречно, вкажи служби: 101 пожежа/ДСНС, 102 поліція, 103 швидка, 104 газ, 112 загальний номер.
            5. Не радь ризиковані дії.
            6. Дай 7-9 коротких кроків українською мовою.

            Поверни тільки валідний JSON без markdown, без пояснень, без ```:
            {
              "title": "$cleanTitle",
              "description": "короткий опис саме цієї НС",
              "steps": [
                "конкретний крок 1",
                "конкретний крок 2",
                "конкретний крок 3",
                "конкретний крок 4",
                "конкретний крок 5",
                "конкретний крок 6",
                "конкретний крок 7"
              ]
            }
        """.trimIndent()

        val body = JSONObject()
            .put("contents", JSONArray().put(content("user", prompt)))
            .put(
                "generationConfig",
                JSONObject()
                    .put("temperature", 0.25)
                    .put("maxOutputTokens", 1200)
            )
            .toString()

        val responseText = requestWithFallbackModels(encodedKey, preferredModel, body)
        parseGeneratedPlan(responseText, cleanTitle, cleanDescription)
    }

    private fun requestWithFallbackModels(encodedKey: String, preferredModel: String, body: String): String {
        val modelsToTry = listOf(
            preferredModel,
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite",
            "gemini-2.0-flash",
            "gemini-1.5-flash"
        ).distinct()

        var lastError: String? = null

        for (modelName in modelsToTry) {
            val result = requestGeminiContent(modelName, encodedKey, body)
            if (result.first) {
                return extractTextFromGeminiResponse(result.second).ifBlank {
                    "Gemini не повернув текстову відповідь."
                }
            }

            lastError = result.second
            val lower = result.second.lowercase()
            val canTryNext = lower.contains("not found") ||
                lower.contains("not supported") ||
                lower.contains("is not found") ||
                lower.contains("model")
            if (!canTryNext) break
        }

        error(lastError ?: "Gemini API error")
    }

    private fun requestGeminiContent(modelName: String, encodedKey: String, body: String): Pair<Boolean, String> {
        val endpoint = URL(
            "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$encodedKey"
        )

        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }

        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(body)
        }

        val responseCode = connection.responseCode
        val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        val response = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }

        Log.d("GeminiTest", "HTTP Code: $responseCode")
        Log.d("GeminiTest", "Gemini response: $response")

        connection.disconnect()

        if (responseCode in 200..299) return true to response

        val message = runCatching {
            JSONObject(response).optJSONObject("error")?.optString("message")
        }.getOrNull()
        return false to (message ?: "Gemini API error: HTTP $responseCode")
    }

    private fun content(role: String, text: String): JSONObject = JSONObject()
        .put("role", role)
        .put("parts", JSONArray().put(JSONObject().put("text", text)))

    private fun extractTextFromGeminiResponse(response: String): String {
        val root = JSONObject(response)
        val candidates = root.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""

        val parts = candidates
            .optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts") ?: return ""

        val result = StringBuilder()
        for (i in 0 until parts.length()) {
            val text = parts.optJSONObject(i)?.optString("text").orEmpty()
            if (text.isNotBlank()) result.append(text).append('\n')
        }
        return result.toString().trim()
    }

    private fun parseGeneratedPlan(rawText: String, fallbackTitle: String, fallbackDescription: String): GeneratedEmergencyPlan {
        val cleaned = rawText
            .replace("```json", "", ignoreCase = true)
            .replace("```", "")
            .trim()
            .let { text ->
                val start = text.indexOf('{')
                val end = text.lastIndexOf('}')
                if (start >= 0 && end > start) text.substring(start, end + 1) else text
            }

        return try {
            val json = JSONObject(cleaned)
            val stepsJson = json.optJSONArray("steps") ?: JSONArray()
            val steps = mutableListOf<String>()

            for (i in 0 until stepsJson.length()) {
                val step = normalizeStep(stepsJson.optString(i))
                if (isUsefulStep(step)) steps.add(step)
            }

            GeneratedEmergencyPlan(
                title = json.optString("title").ifBlank { fallbackTitle }.trim().take(70),
                description = json.optString("description").ifBlank {
                    fallbackDescription.ifBlank { "Власний сценарій, створений AI." }
                }.trim(),
                steps = steps.distinct().take(9).ifEmpty {
                    fallbackCustomSteps(fallbackTitle, fallbackDescription)
                }
            )
        } catch (_: Exception) {
            val lines = rawText
                .lines()
                .map { normalizeStep(it) }
                .filter { isUsefulStep(it) }
                .distinct()
                .take(9)

            GeneratedEmergencyPlan(
                title = fallbackTitle,
                description = fallbackDescription.ifBlank { "Власний сценарій, створений AI." },
                steps = lines.ifEmpty { fallbackCustomSteps(fallbackTitle, fallbackDescription) }
            )
        }
    }

    private fun normalizeStep(text: String): String {
        return text
            .trim()
            .trimStart('-', '•', '*', ' ')
            .replace(Regex("^\\d+[\\.\\)]\\s*"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun isUsefulStep(step: String): Boolean {
        if (step.length < 12) return false
        val lower = step.lowercase()
        val blocked = listOf(
            "{", "}", "[", "]", "\"steps\"", "\"title\"", "\"description\""
        )
        if (blocked.any { lower.contains(it.lowercase()) }) return false

        val tooGeneric = listOf(
            "дій спокійно",
            "не панікуй",
            "оцініть ситуацію",
            "оціни ситуацію"
        )
        return tooGeneric.none { lower == it || lower == "$it." }
    }

    private fun fallbackCustomSteps(title: String, description: String): List<String> {
        val text = "$title $description".lowercase()

        return when {
            text.contains("газ") -> listOf(
                "Не вмикай світло, електроприлади та не користуйся відкритим вогнем.",
                "Відкрий вікна й перекрий газовий кран, якщо це безпечно.",
                "Виведи людей із приміщення та не користуйся ліфтом.",
                "З безпечного місця зателефонуй 104 або 112.",
                "Попередь сусідів без дзвінків у двері та без дій, які можуть створити іскру.",
                "Не повертайся в приміщення до дозволу аварійної служби.",
                "Якщо комусь стало зле, виклич 103."
            )
            text.contains("пожеж") || text.contains("дим") -> listOf(
                "Попередь людей поруч і негайно рухайся до найближчого безпечного виходу.",
                "При задимленні рухайся нижче до підлоги та прикрий рот і ніс тканиною.",
                "Не користуйся ліфтом і не повертайся за речами.",
                "Зачини двері за собою, щоб стримати поширення диму й вогню.",
                "З безпечного місця зателефонуй 101 або 112.",
                "Якщо вихід заблоковано, ущільни двері вологою тканиною й подай сигнал з вікна.",
                "Допомагай постраждалим лише якщо це безпечно для тебе."
            )
            text.contains("землетрус") -> listOf(
                "Відійди від вікон, шаф, полиць і предметів, які можуть впасти.",
                "Сховайся під міцним столом або біля внутрішньої несучої стіни та прикрий голову.",
                "Не користуйся ліфтом і не вибігай сходами під час поштовхів.",
                "Після поштовхів перекрий газ, воду й електрику, якщо це безпечно.",
                "Вийди на відкриту місцевість подалі від будівель і ліній електропередач.",
                "Перевір постраждалих і за потреби виклич 103 або 112.",
                "Не повертайся у пошкоджену будівлю без дозволу рятувальників."
            )
            text.contains("дитин") || text.contains("загуб") -> listOf(
                "Залишайся біля місця, де дитину бачили востаннє, і уточни точний час зникнення.",
                "Перевір найближчі виходи, туалети, ігрові зони, магазини, зупинки або укриття.",
                "Попроси допомоги в охорони, адміністрації або людей поруч.",
                "Зателефонуй 102 або 112, якщо дитина маленька, має ризики або зникла в небезпечному місці.",
                "Передай опис дитини: вік, зріст, одяг, фото й особливі прикмети.",
                "Перевір зв'язок із близькими та знайомими, куди дитина могла піти.",
                "Не поширюй зайві персональні дані дитини у відкритих групах без узгодження з поліцією."
            )
            else -> listOf(
                "Визнач головну небезпеку саме у ситуації: $title.",
                "Відведи себе та людей поруч від джерела ризику на безпечну відстань.",
                "Якщо ризик можна усунути безпечно, зроби це; якщо ні — не наближайся.",
                "За прямої загрози життю або здоров'ю звернися до 101, 102, 103, 104 або 112.",
                "Підготуй телефон, документи, воду, аптечку та необхідні ліки.",
                "Повідом близьких про ситуацію, своє місцезнаходження та план дій.",
                "Дотримуйся офіційних вказівок служб і не поширюй неперевірену інформацію."
            )
        }
    }

    private fun buildSystemPrompt(): String = """
        Ти — AI-помічник мобільного застосунку для автоматизації інформаційної підтримки дій у надзвичайних ситуаціях.
        Відповідай українською мовою, коротко, практично і безпечно.
        У пріоритеті: життя людини, евакуація, виклик служб, базова перша допомога, офіційні інструкції.
        Якщо є загроза життю — рекомендуй 101, 102, 103, 104 або 112.
        Не радь небезпечні дії.
    """.trimIndent()

    private fun buildContextPrompt(
        emergencyType: EmergencyType,
        resources: List<ResourceEntity>,
        checklist: List<ChecklistItemEntity>,
        profile: UserProfile,
        preparednessScore: Int
    ): String {
        val readyResources = resources.filter { it.isAvailable }.joinToString { it.title }.ifBlank { "немає позначених" }
        val missingResources = resources.filterNot { it.isAvailable }.joinToString { it.title }.ifBlank { "немає" }
        val checked = checklist.filter { it.isChecked }.take(12).joinToString { it.title }.ifBlank { "немає" }
        val unchecked = checklist.filterNot { it.isChecked }.take(12).joinToString { it.title }.ifBlank { "немає" }
        val profileText = buildList {
            if (profile.city.isNotBlank()) add("місто: ${profile.city}")
            if (profile.emergencyContact.isNotBlank()) add("екстрений контакт: ${profile.emergencyContact}")
            if (profile.bloodType.isNotBlank()) add("група крові: ${profile.bloodType}")
            if (profile.medicalNotes.isNotBlank()) add("медичні нотатки: ${profile.medicalNotes}")
            if (profile.homeAddressHint.isNotBlank()) add("орієнтир дому: ${profile.homeAddressHint}")
        }.joinToString().ifBlank { "профіль майже не заповнений" }

        return """
            Контекст застосунку:
            - обраний сценарій НС: ${emergencyType.title};
            - рівень готовності користувача: $preparednessScore%;
            - готові ресурси: $readyResources;
            - відсутні ресурси: $missingResources;
            - виконані пункти чек-листа: $checked;
            - невиконані пункти чек-листа: $unchecked;
            - профіль користувача: $profileText.
        """.trimIndent()
    }
}

ІНТЕГРАЦІЯ GOOGLE GEMINI API

Проєкт перероблено з OpenAI API на Google Gemini API.

Що змінено:
1. Замість OpenAiEmergencyAssistant.kt додано GeminiEmergencyAssistant.kt.
2. Запити надсилаються на Google Gemini REST API:
   https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
3. У build.gradle.kts додано читання параметрів:
   GEMINI_API_KEY
   GEMINI_MODEL
4. AI-чат працює як справжній онлайн-AI і відповідає на будь-які питання.
5. Якщо ключ не задано, немає інтернету або API недоступний — автоматично показується резервна локальна рекомендація.

ДЕ ВСТАВИТИ API KEY:
1. Отримайте ключ у Google AI Studio: https://aistudio.google.com/app/apikey
2. Відкрийте файл local.properties у корені проєкту.
3. Додайте рядки:

GEMINI_API_KEY=ваш_ключ_тут
GEMINI_MODEL=gemini-2.5-flash

Після цього в Android Studio виконайте:
1. Sync Project with Gradle Files
2. Build -> Clean Project
3. Build -> Rebuild Project
4. Запустіть застосунок

ВАЖЛИВО:
Не публікуйте local.properties і не вставляйте API-ключ напряму у Kotlin-код.

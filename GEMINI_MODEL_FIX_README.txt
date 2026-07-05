ВИПРАВЛЕННЯ ПОМИЛКИ GEMINI MODEL NOT FOUND

Причина помилки:
У попередній версії було вказано модель gemini-1.5-flash. Для вашого API-ключа ця модель недоступна або вже не підтримується для generateContent у v1beta.

Що виправлено:
1. Основну модель змінено на gemini-2.5-flash.
2. Додано автоматичний fallback: якщо вибрана модель недоступна, застосунок пробує:
   - gemini-2.5-flash
   - gemini-2.5-flash-lite
   - gemini-2.0-flash
3. Локальний резервний режим залишено, щоб застосунок не ламався при проблемах з API.

Що треба вписати у local.properties:

GEMINI_API_KEY=твій_новий_ключ_з_Google_AI_Studio
GEMINI_MODEL=gemini-2.5-flash

Після цього в Android Studio виконай:
1. Sync Project with Gradle Files
2. Build -> Clean Project
3. Build -> Rebuild Project
4. Запусти застосунок заново

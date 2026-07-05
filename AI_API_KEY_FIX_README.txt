Якщо AI показує повідомлення, що Gemini API key is empty, це означає, що застосунок зібрано без Gemini API ключа.

Виправлення:
1. Відкрийте local.properties у корені проєкту.
2. Додайте:

GEMINI_API_KEY=ваш_ключ_тут
GEMINI_MODEL=gemini-2.5-flash

3. Натисніть Sync Project with Gradle Files.
4. Виконайте Clean Project і Rebuild Project.
5. Перевстановіть застосунок на телефоні/емуляторі.

Ключ можна створити у Google AI Studio: https://aistudio.google.com/app/apikey

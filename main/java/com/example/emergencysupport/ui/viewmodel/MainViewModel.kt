package com.example.emergencysupport.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.emergencysupport.BuildConfig
import com.example.emergencysupport.data.ai.GeminiEmergencyAssistant
import androidx.lifecycle.viewModelScope
import com.example.emergencysupport.data.local.entity.ChecklistItemEntity
import com.example.emergencysupport.data.local.entity.CustomEmergencyEntity
import com.example.emergencysupport.data.local.entity.ResourceEntity
import com.example.emergencysupport.data.model.AiChatMessage
import com.example.emergencysupport.data.model.EmergencyType
import com.example.emergencysupport.data.model.GeneratedEmergencyPlan
import com.example.emergencysupport.data.model.ResourceType
import com.example.emergencysupport.data.model.UserProfile
import com.example.emergencysupport.data.repository.EmergencyRepository
import com.example.emergencysupport.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class MainViewModel(
    private val repository: EmergencyRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val selectedType = MutableStateFlow(EmergencyType.POWER_OUTAGE)
    private val _selectedCustomEmergency = MutableStateFlow<CustomEmergencyEntity?>(null)
    private val _isCustomPlanLoading = MutableStateFlow(false)
    private val _customPlanMessage = MutableStateFlow<String?>(null)

    private val _aiMessages = MutableStateFlow(
        listOf(
            AiChatMessage(
                text = "Привіт! Я Gemini AI-помічник з надзвичайних ситуацій. Обери сценарій або напиши питання, наприклад: ‘що робити при пожежі?’, ‘що покласти в тривожну валізу?’ чи ‘як діяти при витоку газу?’. ",
                isUser = false
            )
        )
    )

    val resources = repository.getResources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val checklist = repository.getChecklist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val customEmergencies = repository.getCustomEmergencies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val aiAssistant = GeminiEmergencyAssistant(
        apiKey = BuildConfig.GEMINI_API_KEY,
        model = BuildConfig.GEMINI_MODEL
    )
    private val _isAiLoading = MutableStateFlow(false)

    val selectedEmergencyType: StateFlow<EmergencyType> = selectedType
    val selectedCustomEmergency: StateFlow<CustomEmergencyEntity?> = _selectedCustomEmergency
    val isCustomPlanLoading: StateFlow<Boolean> = _isCustomPlanLoading
    val customPlanMessage: StateFlow<String?> = _customPlanMessage
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages
    val isAiLoading: StateFlow<Boolean> = _isAiLoading

    val isFirstLaunch = settingsRepository.isFirstLaunch
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val userProfile = settingsRepository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProfile())

    val preparednessScore = combine(resources, checklist) { resourceList, checklistItems ->
        val resourcePart = if (resourceList.isEmpty()) 0.0 else resourceList.count { it.isAvailable }.toDouble() / resourceList.size
        val checklistPart = if (checklistItems.isEmpty()) 0.0 else checklistItems.count { it.isChecked }.toDouble() / checklistItems.size
        (((resourcePart * 0.55) + (checklistPart * 0.45)) * 100).roundToInt()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val checkedChecklistCount = checklist
        .map { items -> items.count { it.isChecked } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalChecklistCount = checklist
        .map { items -> items.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val availableResourcesCount = resources
        .map { items -> items.count { it.isAvailable } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalResourcesCount = resources
        .map { items -> items.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val missingCriticalResources = combine(selectedType, resources) { type, resourceList ->
        criticalResourcesFor(type)
            .filterNot { critical -> resourceList.any { it.key == critical.name && it.isAvailable } }
            .map { it.title }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val smartAdvice = combine(selectedType, resources, checklist, userProfile) { type, resourceList, checklistItems, profile ->
        buildAdvice(type, resourceList, checklistItems, profile)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recommendedKit = combine(selectedType, resources, userProfile) { type, resourceList, profile ->
        buildRecommendedKit(type, resourceList, profile)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val profileCompleteness = userProfile
        .map { profile ->
            listOf(
                profile.fullName,
                profile.email,
                profile.city,
                profile.emergencyContact,
                profile.bloodType,
                profile.medicalNotes,
                profile.homeAddressHint
            ).count { it.isNotBlank() } * 100 / 7
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
        }
    }


    fun askAi(question: String) {
        val cleanQuestion = question.trim()
        if (cleanQuestion.isBlank() || _isAiLoading.value) return

        val previousMessages = _aiMessages.value.map { it.isUser to it.text }
        _aiMessages.value = _aiMessages.value + AiChatMessage(cleanQuestion, isUser = true)

        viewModelScope.launch {
            _isAiLoading.value = true

            val onlineAnswer = withContext(Dispatchers.IO) {
                aiAssistant.ask(
                    question = cleanQuestion,
                    emergencyType = selectedType.value,
                    resources = resources.value,
                    checklist = checklist.value,
                    profile = userProfile.value,
                    preparednessScore = preparednessScore.value,
                    previousMessages = previousMessages
                )
            }

            val answer = onlineAnswer.getOrElse { error ->
                val localAnswer = buildAiAnswer(
                    question = cleanQuestion,
                    type = selectedType.value,
                    resourceList = resources.value,
                    checklistItems = checklist.value,
                    profile = userProfile.value,
                    score = preparednessScore.value
                )
                "⚠️ Не вдалося отримати відповідь від Gemini AI: ${error.message ?: "невідома помилка"}.\n\nПоказую резервну локальну рекомендацію застосунку:\n\n$localAnswer"
            }

            _aiMessages.value = _aiMessages.value + AiChatMessage(answer, isUser = false)
            _isAiLoading.value = false
        }
    }

    fun clearAiChat() {
        _aiMessages.value = listOf(
            AiChatMessage(
                text = "Чат очищено. Напиши будь-яке питання. Якщо вказано GEMINI_API_KEY, відповідь надасть справжній Gemini AI; без ключа спрацює резервний локальний режим.",
                isUser = false
            )
        )
    }

    fun selectEmergencyType(type: EmergencyType) {
        _selectedCustomEmergency.value = null
        selectedType.value = type
    }

    fun selectCustomEmergency(item: CustomEmergencyEntity) {
        _selectedCustomEmergency.value = item
    }

    fun deleteCustomEmergency(item: CustomEmergencyEntity) {
        viewModelScope.launch {
            repository.deleteCustomEmergency(item)
            if (_selectedCustomEmergency.value?.id == item.id) {
                _selectedCustomEmergency.value = null
                selectedType.value = EmergencyType.POWER_OUTAGE
            }
            _customPlanMessage.value = "Ситуацію «${item.title}» видалено."
        }
    }

    fun toggleResource(resource: ResourceEntity) {
        viewModelScope.launch {
            repository.toggleResource(resource)
        }
    }

    fun toggleChecklistItem(item: ChecklistItemEntity) {
        viewModelScope.launch {
            repository.toggleChecklistItem(item)
        }
    }

    fun addChecklistItem(title: String, category: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addChecklistItem(title.trim(), category.ifBlank { "Власне" }.trim())
        }
    }

    fun editChecklistItem(item: ChecklistItemEntity, title: String, category: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.updateChecklistItem(item, title.trim(), category.ifBlank { "Власне" }.trim())
        }
    }

    fun deleteChecklistItem(item: ChecklistItemEntity) {
        viewModelScope.launch {
            repository.deleteChecklistItem(item)
        }
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            settingsRepository.finishOnboarding()
        }
    }

    fun login(profile: UserProfile) {
        viewModelScope.launch {
            settingsRepository.login(profile)
        }
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            settingsRepository.updateProfile(profile)
        }
    }

    fun logout() {
        viewModelScope.launch {
            settingsRepository.logout()
        }
    }

    fun getPlanFlow(type: EmergencyType) = repository.getPlanByType(type.name)

    fun getPlanFlowByKey(planKey: String) = repository.getPlanByType(planKey)

    fun createCustomEmergency(title: String, description: String) {
        val cleanTitle = title.trim()
        val cleanDescription = description.trim()
        if (cleanTitle.isBlank() || _isCustomPlanLoading.value) return

        viewModelScope.launch {
            _isCustomPlanLoading.value = true
            _customPlanMessage.value = "AI продумує кроки для нової надзвичайної ситуації..."

            val generated = withContext(Dispatchers.IO) {
                aiAssistant.generateCustomEmergencyPlan(
                    title = cleanTitle,
                    description = cleanDescription,
                    resources = resources.value,
                    checklist = checklist.value,
                    profile = userProfile.value,
                    preparednessScore = preparednessScore.value
                )
            }.getOrElse {
                buildFallbackGeneratedPlan(cleanTitle, cleanDescription)
            }

            val safeSteps = generated.steps
                .map { it.trim() }
                .filter { it.length >= 8 }
                .ifEmpty { buildFallbackGeneratedPlan(cleanTitle, cleanDescription).steps }

            val saved = repository.addCustomEmergency(
                title = generated.title.ifBlank { cleanTitle },
                description = generated.description.ifBlank { cleanDescription.ifBlank { "Власна надзвичайна ситуація." } },
                steps = safeSteps
            )

            _selectedCustomEmergency.value = saved
            _customPlanMessage.value = "Ситуацію «${saved.title}» додано у План і на Головну. Створено ${safeSteps.size} кроків."
            _isCustomPlanLoading.value = false
        }
    }

    private fun buildFallbackGeneratedPlan(title: String, description: String): GeneratedEmergencyPlan {
        val safeDescription = description.ifBlank { "Власна надзвичайна ситуація користувача." }
        val text = "$title $description".lowercase()
        val steps = when {
            text.contains("газ") -> listOf(
                "Не вмикай світло, електроприлади та не користуйся відкритим вогнем.",
                "Відкрий вікна й перекрий газовий кран, якщо це безпечно.",
                "Виведи людей із приміщення та не користуйся ліфтом.",
                "З безпечного місця зателефонуй 104 або 112.",
                "Попередь сусідів без дій, які можуть створити іскру.",
                "Не повертайся всередину до дозволу аварійної служби.",
                "Якщо комусь стало зле, виклич 103."
            )
            text.contains("пожеж") || text.contains("дим") -> listOf(
                "Попередь людей поруч і негайно рухайся до безпечного виходу.",
                "При задимленні рухайся нижче до підлоги та прикрий рот і ніс тканиною.",
                "Не користуйся ліфтом і не повертайся за речами.",
                "Зачини двері за собою, щоб стримати дим і вогонь.",
                "З безпечного місця зателефонуй 101 або 112.",
                "Якщо вихід заблоковано, ущільни двері вологою тканиною і подай сигнал з вікна.",
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
                "Залишайся біля місця, де дитину бачили востаннє, і уточни час зникнення.",
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

        return GeneratedEmergencyPlan(title = title, description = safeDescription, steps = steps)
    }

    private fun buildAiAnswer(
        question: String,
        type: EmergencyType,
        resourceList: List<ResourceEntity>,
        checklistItems: List<ChecklistItemEntity>,
        profile: UserProfile,
        score: Int
    ): String {
        val q = question.lowercase()
        val available = resourceList.filter { it.isAvailable }
        val missing = criticalResourcesFor(type)
            .filterNot { critical -> resourceList.any { it.key == critical.name && it.isAvailable } }
            .map { it.title }
        val unchecked = checklistItems.filterNot { it.isChecked }.take(3).map { it.title }
        val personalNotes = mutableListOf<String>()

        if (profile.city.isNotBlank()) personalNotes += "місто: ${profile.city}"
        if (profile.emergencyContact.isNotBlank()) personalNotes += "екстрений контакт: ${profile.emergencyContact}"
        if (profile.medicalNotes.isNotBlank()) personalNotes += "медичні нотатки враховано"

        val mainAdvice = when {
            q.contains("газ") || type == EmergencyType.GAS_LEAK -> listOf(
                "Не вмикай і не вимикай світло, не користуйся відкритим вогнем.",
                "Перекрий газ, якщо це безпечно, відкрий вікна та вийди з приміщення.",
                "Зателефонуй 104 або 101 вже з безпечного місця."
            )
            q.contains("пожеж") || q.contains("дим") || type == EmergencyType.FIRE -> listOf(
                "Головний пріоритет — евакуація людей, а не речі.",
                "Якщо є дим, рухайся нижче до підлоги та закрий рот і ніс тканиною.",
                "Не користуйся ліфтом і телефонуй 101 після виходу у безпечне місце."
            )
            q.contains("тривож") || q.contains("валіз") || q.contains("рюкзак") || q.contains("набір") || q.contains("ресурс") -> listOf(
                "Базовий набір: документи, вода, аптечка, павербанк, ліхтарик, їжа, готівка, засоби гігієни.",
                "Для кожної людини бажано мати запас води, індивідуальні ліки та копії документів.",
                "Перевір заряд пристроїв і поклади речі так, щоб їх можна було взяти за 1–2 хвилини."
            )
            q.contains("укрит") || q.contains("тривог") || type == EmergencyType.AIR_ALERT -> listOf(
                "Після сигналу тривоги переходь в укриття або правило двох стін.",
                "Візьми телефон, документи, воду, ліки та теплий одяг.",
                "Не ігноруй повторні сигнали та не підходь до вікон."
            )
            q.contains("евак") || type == EmergencyType.EVACUATION -> listOf(
                "Підготуй маршрут, запасний маршрут і точку зустрічі з близькими.",
                "Візьми документи, аптечку, воду, готівку, зарядні пристрої та мінімум речей.",
                "Повідом близьким, куди рухаєшся, і не повертайся в небезпечну зону без дозволу служб."
            )
            q.contains("аптеч") || q.contains("допомог") || q.contains("кров") || type == EmergencyType.FIRST_AID -> listOf(
                "Оціни безпеку місця, виклич 103 і тільки після цього надавай допомогу.",
                "При сильній кровотечі тисни на рану чистою тканиною або бинтом.",
                "Не давай людині ліки, якщо не впевнена у показаннях або є ризик алергії."
            )
            q.contains("вода") || type == EmergencyType.WATER_OUTAGE -> listOf(
                "Підготуй питну воду окремо від технічної.",
                "Набери воду у чисті ємності, ванну або відра для побутових потреб.",
                "Використовуй воду економно та перевір офіційні повідомлення щодо підвозу води."
            )
            q.contains("світ") || q.contains("елект") || type == EmergencyType.POWER_OUTAGE -> listOf(
                "Заряди телефон і павербанк, підготуй ліхтарик або лампу на батарейках.",
                "Не використовуй генератор у квартирі чи закритому приміщенні.",
                "Відключи чутливу техніку від розеток, щоб уникнути пошкодження після відновлення електрики."
            )
            else -> listOf(
                "Для сценарію ‘${type.title}’ дій спокійно: спочатку безпека людей, потім документи та речі.",
                "Оціни ризик, виконай базові кроки плану та звертайся до офіційних служб за потреби.",
                "Підготуй ресурси, які застосунок позначає як критичні для цього сценарію."
            )
        }

        val readinessLine = "Поточна готовність: $score%. Готових ресурсів: ${available.size}/${resourceList.size}, виконано пунктів чек-листа: ${checklistItems.count { it.isChecked }}/${checklistItems.size}."
        val missingLine = if (missing.isNotEmpty()) "Критично додати: ${missing.joinToString()}." else "Критичні ресурси для цього сценарію позначені як готові."
        val checklistLine = if (unchecked.isNotEmpty()) "Найближчі пункти чек-листа: ${unchecked.joinToString()}." else "Усі основні пункти чек-листа виконані."
        val profileLine = if (personalNotes.isNotEmpty()) "Персональні дані враховано (${personalNotes.joinToString()})." else "Заповни профіль: місто, контакт близької людини та медичні нотатки — AI-поради стануть точнішими."

        return (mainAdvice.mapIndexed { index, item -> "${index + 1}. $item" } + listOf(readinessLine, missingLine, checklistLine, profileLine))
            .joinToString("\n")
    }

    private fun criticalResourcesFor(type: EmergencyType): List<ResourceType> = when (type) {
        EmergencyType.POWER_OUTAGE -> listOf(ResourceType.FLASHLIGHT, ResourceType.POWERBANK, ResourceType.WATER_SUPPLY)
        EmergencyType.FIRE -> listOf(ResourceType.FIRST_AID_KIT, ResourceType.DOCUMENTS, ResourceType.WHISTLE)
        EmergencyType.AIR_ALERT -> listOf(ResourceType.DOCUMENTS, ResourceType.WATER_SUPPLY, ResourceType.RADIO)
        EmergencyType.WATER_OUTAGE -> listOf(ResourceType.WATER_SUPPLY, ResourceType.HYGIENE)
        EmergencyType.FIRST_AID -> listOf(ResourceType.FIRST_AID_KIT, ResourceType.MEDICINES)
        EmergencyType.FLOOD -> listOf(ResourceType.DOCUMENTS, ResourceType.WATER_SUPPLY, ResourceType.CAR)
        EmergencyType.CHEMICAL_HAZARD -> listOf(ResourceType.RESPIRATOR, ResourceType.RADIO, ResourceType.WATER_SUPPLY)
        EmergencyType.GAS_LEAK -> listOf(ResourceType.DOCUMENTS, ResourceType.WHISTLE)
        EmergencyType.WINTER_STORM -> listOf(ResourceType.BLANKET, ResourceType.FOOD_SUPPLY, ResourceType.POWERBANK)
        EmergencyType.EVACUATION -> listOf(ResourceType.DOCUMENTS, ResourceType.FIRST_AID_KIT, ResourceType.WATER_SUPPLY)
    }

    private fun buildAdvice(
        type: EmergencyType,
        resourceList: List<ResourceEntity>,
        checklistItems: List<ChecklistItemEntity>,
        profile: UserProfile
    ): List<String> {
        val availableKeys = resourceList.filter { it.isAvailable }.map { it.key }.toSet()
        val checkedItems = checklistItems.count { it.isChecked }
        val baseAdvice = mutableListOf<String>()

        baseAdvice += when (type) {
            EmergencyType.POWER_OUTAGE -> "Тримай телефон зарядженим і підготуй автономне освітлення."
            EmergencyType.FIRE -> "Пріоритет — швидкий вихід, а не збір речей."
            EmergencyType.AIR_ALERT -> "Заздалегідь знай місце укриття та час підходу до нього."
            EmergencyType.WATER_OUTAGE -> "Підготуй запас як питної, так і технічної води."
            EmergencyType.FIRST_AID -> "Перевір, чи є індивідуальні ліки для всіх членів родини."
            EmergencyType.FLOOD -> "Тримай документи та електроніку вище рівня можливого затоплення."
            EmergencyType.CHEMICAL_HAZARD -> "Для цього сценарію критично важливі ізоляція приміщення та захист дихання."
            EmergencyType.GAS_LEAK -> "Не використовуй вимикачі та не запалюй вогонь у зоні витоку."
            EmergencyType.WINTER_STORM -> "Автономне тепло і заряд зв'язку важливіші за комфорт."
            EmergencyType.EVACUATION -> "Найкраща евакуація — та, до якої маршрут і речі підготовлені заздалегідь."
        }

        if (profile.emergencyContact.isBlank()) {
            baseAdvice += "Додай контакт близької людини — це покращить готовність у будь-якому сценарії."
        }
        if (profile.medicalNotes.isBlank()) {
            baseAdvice += "Заповни медичні нотатки у профілі: алергії, хронічні стани або важливі ліки."
        }
        if (ResourceType.POWERBANK.name !in availableKeys) {
            baseAdvice += "Додай павербанк — це підвищить автономність майже для будь-якої ситуації."
        }
        if (ResourceType.FIRST_AID_KIT.name !in availableKeys) {
            baseAdvice += "Аптечка досі не відмічена як готова — це один із найважливіших ресурсів."
        }
        if (checkedItems < 5) {
            baseAdvice += "У чек-листі поки мало виконаних пунктів — варто почати з базового набору: вода, їжа, світло, документи."
        }

        return baseAdvice.distinct().take(5)
    }

    private fun buildRecommendedKit(
        type: EmergencyType,
        resourceList: List<ResourceEntity>,
        profile: UserProfile
    ): List<String> {
        val available = resourceList.filter { it.isAvailable }.map { it.title }.toSet()
        val kit = mutableListOf<String>()
        kit += when (type) {
            EmergencyType.POWER_OUTAGE -> listOf("Ліхтарик", "Павербанк", "Радіо", "Запас води")
            EmergencyType.FIRE -> listOf("Копії документів", "Свисток", "Аптечка", "Контакт для збору")
            EmergencyType.AIR_ALERT -> listOf("Документи", "Вода", "Аптечка", "Радіо")
            EmergencyType.WATER_OUTAGE -> listOf("Запас води", "Засоби гігієни", "Ємності для набору")
            EmergencyType.FIRST_AID -> listOf("Аптечка", "Необхідні ліки", "Медичні нотатки")
            EmergencyType.FLOOD -> listOf("Документи", "Вода", "Їжа", "План евакуації")
            EmergencyType.CHEMICAL_HAZARD -> listOf("Респіратор", "Радіо", "Вода", "Ізоляційна стрічка")
            EmergencyType.GAS_LEAK -> listOf("Документи", "Свисток", "Контакти сусідів")
            EmergencyType.WINTER_STORM -> listOf("Плед", "Їжа", "Павербанк", "Теплий одяг")
            EmergencyType.EVACUATION -> listOf("Документи", "Аптечка", "Вода", "Готівка")
        }
        return kit
            .filterNot { item -> available.any { ready -> item.contains(ready, ignoreCase = true) } }
            .distinct()
            .take(6)
    }
}

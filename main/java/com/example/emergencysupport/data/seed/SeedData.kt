package com.example.emergencysupport.data.seed

import com.example.emergencysupport.data.local.entity.ChecklistItemEntity
import com.example.emergencysupport.data.local.entity.EmergencyPlanEntity
import com.example.emergencysupport.data.local.entity.ResourceEntity
import com.example.emergencysupport.data.model.EmergencyType
import com.example.emergencysupport.data.model.ResourceType

object SeedData {
    fun defaultResources(): List<ResourceEntity> = listOf(
        ResourceEntity(key = ResourceType.GENERATOR.name, title = ResourceType.GENERATOR.title, isAvailable = false),
        ResourceEntity(key = ResourceType.CAR.name, title = ResourceType.CAR.title, isAvailable = false),
        ResourceEntity(key = ResourceType.FIRST_AID_KIT.name, title = ResourceType.FIRST_AID_KIT.title, isAvailable = false),
        ResourceEntity(key = ResourceType.FLASHLIGHT.name, title = ResourceType.FLASHLIGHT.title, isAvailable = false),
        ResourceEntity(key = ResourceType.POWERBANK.name, title = ResourceType.POWERBANK.title, isAvailable = false),
        ResourceEntity(key = ResourceType.WATER_SUPPLY.name, title = ResourceType.WATER_SUPPLY.title, isAvailable = false),
        ResourceEntity(key = ResourceType.FOOD_SUPPLY.name, title = ResourceType.FOOD_SUPPLY.title, isAvailable = false),
        ResourceEntity(key = ResourceType.RADIO.name, title = ResourceType.RADIO.title, isAvailable = false),
        ResourceEntity(key = ResourceType.DOCUMENTS.name, title = ResourceType.DOCUMENTS.title, isAvailable = false),
        ResourceEntity(key = ResourceType.MEDICINES.name, title = ResourceType.MEDICINES.title, isAvailable = false),
        ResourceEntity(key = ResourceType.BLANKET.name, title = ResourceType.BLANKET.title, isAvailable = false),
        ResourceEntity(key = ResourceType.RESPIRATOR.name, title = ResourceType.RESPIRATOR.title, isAvailable = false),
        ResourceEntity(key = ResourceType.WHISTLE.name, title = ResourceType.WHISTLE.title, isAvailable = false),
        ResourceEntity(key = ResourceType.HYGIENE.name, title = ResourceType.HYGIENE.title, isAvailable = false)
    )

    fun defaultPlans(): List<EmergencyPlanEntity> = listOf(
        EmergencyPlanEntity(emergencyType = EmergencyType.POWER_OUTAGE.name, stepOrder = 1, stepText = "Перевірити, чи відключення стосується лише вашого приміщення."),
        EmergencyPlanEntity(emergencyType = EmergencyType.POWER_OUTAGE.name, stepOrder = 2, stepText = "Увімкнути ліхтарик або інше резервне джерело світла.", requiredResourceKey = ResourceType.FLASHLIGHT.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.POWER_OUTAGE.name, stepOrder = 3, stepText = "Забезпечити заряд телефону через павербанк.", requiredResourceKey = ResourceType.POWERBANK.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.POWER_OUTAGE.name, stepOrder = 4, stepText = "Підготувати запас води та їжі на кілька годин.", requiredResourceKey = ResourceType.WATER_SUPPLY.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.POWER_OUTAGE.name, stepOrder = 5, stepText = "За наявності генератора підготувати його до безпечного використання.", requiredResourceKey = ResourceType.GENERATOR.name),

        EmergencyPlanEntity(emergencyType = EmergencyType.FIRE.name, stepOrder = 1, stepText = "Негайно оцінити джерело займання та не панікувати."),
        EmergencyPlanEntity(emergencyType = EmergencyType.FIRE.name, stepOrder = 2, stepText = "За можливості залишити приміщення без використання ліфта."),
        EmergencyPlanEntity(emergencyType = EmergencyType.FIRE.name, stepOrder = 3, stepText = "Триматися нижче рівня диму та закрити двері за собою."),
        EmergencyPlanEntity(emergencyType = EmergencyType.FIRE.name, stepOrder = 4, stepText = "Взяти аптечку лише якщо це безпечно.", requiredResourceKey = ResourceType.FIRST_AID_KIT.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.FIRE.name, stepOrder = 5, stepText = "Після виходу у безпечне місце зателефонувати 101."),

        EmergencyPlanEntity(emergencyType = EmergencyType.AIR_ALERT.name, stepOrder = 1, stepText = "Негайно пройти до укриття або безпечного місця."),
        EmergencyPlanEntity(emergencyType = EmergencyType.AIR_ALERT.name, stepOrder = 2, stepText = "Взяти телефон, документи, воду та аптечку, якщо це не затримує евакуацію.", requiredResourceKey = ResourceType.DOCUMENTS.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.AIR_ALERT.name, stepOrder = 3, stepText = "Слідкувати за офіційними повідомленнями або радіо.", requiredResourceKey = ResourceType.RADIO.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.AIR_ALERT.name, stepOrder = 4, stepText = "Не залишати укриття до офіційного відбою."),

        EmergencyPlanEntity(emergencyType = EmergencyType.WATER_OUTAGE.name, stepOrder = 1, stepText = "Перевірити запас питної та технічної води.", requiredResourceKey = ResourceType.WATER_SUPPLY.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.WATER_OUTAGE.name, stepOrder = 2, stepText = "Обмежити використання води до критично необхідного мінімуму."),
        EmergencyPlanEntity(emergencyType = EmergencyType.WATER_OUTAGE.name, stepOrder = 3, stepText = "Підготувати чисті ємності для набору води після відновлення постачання."),
        EmergencyPlanEntity(emergencyType = EmergencyType.WATER_OUTAGE.name, stepOrder = 4, stepText = "Перевірити наявність засобів гігієни.", requiredResourceKey = ResourceType.HYGIENE.name),

        EmergencyPlanEntity(emergencyType = EmergencyType.FIRST_AID.name, stepOrder = 1, stepText = "Оцінити стан постраждалого та усунути небезпечні фактори."),
        EmergencyPlanEntity(emergencyType = EmergencyType.FIRST_AID.name, stepOrder = 2, stepText = "Надати первинну допомогу, використовуючи аптечку.", requiredResourceKey = ResourceType.FIRST_AID_KIT.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.FIRST_AID.name, stepOrder = 3, stepText = "Перевірити необхідні ліки та індивідуальні потреби.", requiredResourceKey = ResourceType.MEDICINES.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.FIRST_AID.name, stepOrder = 4, stepText = "За необхідності викликати 103."),

        EmergencyPlanEntity(emergencyType = EmergencyType.FLOOD.name, stepOrder = 1, stepText = "Перемістити документи та цінні речі вище рівня можливого затоплення.", requiredResourceKey = ResourceType.DOCUMENTS.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.FLOOD.name, stepOrder = 2, stepText = "Підготувати воду, їжу та аптечку на випадок евакуації.", requiredResourceKey = ResourceType.WATER_SUPPLY.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.FLOOD.name, stepOrder = 3, stepText = "Відключити електроживлення, якщо є ризик контакту води з проводкою."),
        EmergencyPlanEntity(emergencyType = EmergencyType.FLOOD.name, stepOrder = 4, stepText = "У разі наказу на евакуацію вирушати негайно, не чекаючи погіршення ситуації."),

        EmergencyPlanEntity(emergencyType = EmergencyType.CHEMICAL_HAZARD.name, stepOrder = 1, stepText = "Зайти в приміщення та щільно зачинити вікна і двері."),
        EmergencyPlanEntity(emergencyType = EmergencyType.CHEMICAL_HAZARD.name, stepOrder = 2, stepText = "Використати маску або респіратор для захисту органів дихання.", requiredResourceKey = ResourceType.RESPIRATOR.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.CHEMICAL_HAZARD.name, stepOrder = 3, stepText = "Слідкувати за офіційними повідомленнями про напрямок вітру та зону ураження.", requiredResourceKey = ResourceType.RADIO.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.CHEMICAL_HAZARD.name, stepOrder = 4, stepText = "Підготувати воду та аптечку на випадок тривалого перебування в укритті."),

        EmergencyPlanEntity(emergencyType = EmergencyType.GAS_LEAK.name, stepOrder = 1, stepText = "Не вмикати світло та не користуватися електроприладами чи відкритим вогнем."),
        EmergencyPlanEntity(emergencyType = EmergencyType.GAS_LEAK.name, stepOrder = 2, stepText = "Перекрити газ, якщо це можна зробити безпечно."),
        EmergencyPlanEntity(emergencyType = EmergencyType.GAS_LEAK.name, stepOrder = 3, stepText = "Відкрити вікна для провітрювання та залишити приміщення."),
        EmergencyPlanEntity(emergencyType = EmergencyType.GAS_LEAK.name, stepOrder = 4, stepText = "Попередити інших мешканців і зателефонувати аварійній службі 104."),

        EmergencyPlanEntity(emergencyType = EmergencyType.WINTER_STORM.name, stepOrder = 1, stepText = "Перевірити теплий одяг, пледи та автономне освітлення.", requiredResourceKey = ResourceType.BLANKET.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.WINTER_STORM.name, stepOrder = 2, stepText = "Зарядити телефони, павербанк та підготувати запас їжі.", requiredResourceKey = ResourceType.POWERBANK.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.WINTER_STORM.name, stepOrder = 3, stepText = "Обмежити вихід на вулицю без нагальної потреби."),
        EmergencyPlanEntity(emergencyType = EmergencyType.WINTER_STORM.name, stepOrder = 4, stepText = "Підготувати резервне джерело тепла або безпечний план переміщення."),

        EmergencyPlanEntity(emergencyType = EmergencyType.EVACUATION.name, stepOrder = 1, stepText = "Зібрати документи, гроші, телефон і зарядні пристрої.", requiredResourceKey = ResourceType.DOCUMENTS.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.EVACUATION.name, stepOrder = 2, stepText = "Взяти воду, базовий запас їжі та аптечку.", requiredResourceKey = ResourceType.FIRST_AID_KIT.name),
        EmergencyPlanEntity(emergencyType = EmergencyType.EVACUATION.name, stepOrder = 3, stepText = "Перевірити маршрут виходу та місце збору родини."),
        EmergencyPlanEntity(emergencyType = EmergencyType.EVACUATION.name, stepOrder = 4, stepText = "Вирушати без зволікань, якщо отримано офіційну команду або є прямий ризик."),
        EmergencyPlanEntity(emergencyType = EmergencyType.EVACUATION.name, stepOrder = 5, stepText = "За можливості взяти свисток або інший засіб сигналу.", requiredResourceKey = ResourceType.WHISTLE.name)
    )

    fun defaultChecklist(): List<ChecklistItemEntity> = listOf(
        ChecklistItemEntity(category = "Базова підготовка", title = "Підготувати аптечку"),
        ChecklistItemEntity(category = "Базова підготовка", title = "Підготувати запас води"),
        ChecklistItemEntity(category = "Базова підготовка", title = "Підготувати запас їжі"),
        ChecklistItemEntity(category = "Базова підготовка", title = "Перевірити ліхтарик"),
        ChecklistItemEntity(category = "Базова підготовка", title = "Зарядити павербанк"),
        ChecklistItemEntity(category = "Документи та зв'язок", title = "Підготувати копії важливих документів"),
        ChecklistItemEntity(category = "Документи та зв'язок", title = "Записати екстрені номери офлайн"),
        ChecklistItemEntity(category = "Медицина", title = "Покласти необхідні ліки"),
        ChecklistItemEntity(category = "Медицина", title = "Перевірити терміни придатності аптечки"),
        ChecklistItemEntity(category = "Дім", title = "Знати, де перекривається газ та вода"),
        ChecklistItemEntity(category = "Дім", title = "Підготувати теплий одяг або плед"),
        ChecklistItemEntity(category = "Евакуація", title = "Підготувати базову тривожну валізу"),
        ChecklistItemEntity(category = "Евакуація", title = "Продумати маршрут евакуації"),
        ChecklistItemEntity(category = "Евакуація", title = "Узгодити місце зустрічі з близькими")
    )
}

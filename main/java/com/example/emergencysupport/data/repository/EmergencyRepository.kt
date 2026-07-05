package com.example.emergencysupport.data.repository

import com.example.emergencysupport.data.local.dao.ChecklistItemDao
import com.example.emergencysupport.data.local.dao.CustomEmergencyDao
import com.example.emergencysupport.data.local.dao.EmergencyPlanDao
import com.example.emergencysupport.data.local.dao.ResourceDao
import com.example.emergencysupport.data.local.entity.ChecklistItemEntity
import com.example.emergencysupport.data.local.entity.CustomEmergencyEntity
import com.example.emergencysupport.data.local.entity.EmergencyPlanEntity
import com.example.emergencysupport.data.local.entity.ResourceEntity
import com.example.emergencysupport.data.seed.SeedData
import kotlinx.coroutines.flow.Flow

class EmergencyRepository(
    private val resourceDao: ResourceDao,
    private val planDao: EmergencyPlanDao,
    private val checklistItemDao: ChecklistItemDao,
    private val customEmergencyDao: CustomEmergencyDao
) {
    fun getResources(): Flow<List<ResourceEntity>> = resourceDao.getAllResources()
    fun getPlanByType(type: String) = planDao.getPlanByType(type)
    fun getChecklist() = checklistItemDao.getAllChecklistItems()
    fun getCustomEmergencies(): Flow<List<CustomEmergencyEntity>> = customEmergencyDao.getCustomEmergencies()

    suspend fun toggleResource(resource: ResourceEntity) {
        resourceDao.updateResource(resource.copy(isAvailable = !resource.isAvailable))
    }

    suspend fun toggleChecklistItem(item: ChecklistItemEntity) {
        checklistItemDao.updateItem(item.copy(isChecked = !item.isChecked))
    }

    suspend fun addChecklistItem(title: String, category: String) {
        checklistItemDao.insertItem(ChecklistItemEntity(title = title, category = category))
    }

    suspend fun updateChecklistItem(item: ChecklistItemEntity, title: String, category: String) {
        checklistItemDao.updateItem(item.copy(title = title, category = category))
    }

    suspend fun deleteChecklistItem(item: ChecklistItemEntity) {
        checklistItemDao.deleteItem(item)
    }


    suspend fun addCustomEmergency(title: String, description: String, steps: List<String>): CustomEmergencyEntity {
        val key = "CUSTOM_" + System.currentTimeMillis()
        val scenario = CustomEmergencyEntity(
            planKey = key,
            title = title.trim(),
            description = description.trim()
        )

        val insertedId = customEmergencyDao.insertCustomEmergency(scenario).toInt()

        val planItems = steps
            .filter { it.isNotBlank() }
            .mapIndexed { index, step ->
                EmergencyPlanEntity(
                    emergencyType = key,
                    stepOrder = index + 1,
                    stepText = step.trim()
                )
            }

        planDao.deletePlanByType(key)
        planDao.insertAll(planItems)

        return scenario.copy(id = insertedId)
    }

    suspend fun deleteCustomEmergency(item: CustomEmergencyEntity) {
        planDao.deletePlanByType(item.planKey)
        customEmergencyDao.deleteCustomEmergencyById(item.id)
    }

    suspend fun seedIfNeeded() {
        if (resourceDao.countResources() == 0) resourceDao.insertAll(SeedData.defaultResources())
        if (planDao.countPlans() == 0) planDao.insertAll(SeedData.defaultPlans())
        if (checklistItemDao.countItems() == 0) checklistItemDao.insertAll(SeedData.defaultChecklist())
    }
}

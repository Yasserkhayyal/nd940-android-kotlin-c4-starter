package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    private val reminderList = mutableListOf<ReminderDTO>()
    var shouldShowError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> =
        if (shouldShowError) {
            Result.Error("Test exception")
        } else {
            Result.Success(reminderList)
        }


    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if (shouldShowError) {
            Result.Error("Test Exception")
        } else {
            try {
                Result.Success(reminderList.first { it.id == id })
            } catch (e: Exception) {
                Result.Error(e.message)
            }
        }
    }

    override suspend fun deleteAllReminders() {
        reminderList.clear()
    }
}
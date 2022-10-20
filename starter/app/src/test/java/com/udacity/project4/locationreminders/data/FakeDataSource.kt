package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    val reminderList = mutableListOf<ReminderDTO>()
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

    override suspend fun updateReminder(reminder: ReminderDTO) {
        reminderList.find { reminder.id == it.id }?.apply {
            val index = reminderList.indexOf(this)
            reminderList.remove(this)
            reminderList.add(index, reminder)
        }
    }

    override suspend fun deleteReminder(reminder: ReminderDTO) {
        reminderList.remove(reminder)
    }

    override suspend fun getReminder(id: Int): Result<ReminderDTO> {
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
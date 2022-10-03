package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        localDataSource = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveReminder_return_Expected_Value() = runBlocking {
        // GIVEN - A new task saved in the database.
        val reminder = ReminderDTO(
            title = "title",
            description = "description",
            location = "Landmark",
            latitude = 25.3,
            longitude = 17.5
        )
        localDataSource.saveReminder(reminder)

        // WHEN  - Task retrieved by ID.
        val result = localDataSource.getReminder(reminder.id)

        // THEN - Same task is returned.
        result as Result.Success
        //couldn't use kluent here, so resorted back to Junit assertion
        assertEquals(result.data.title, reminder.title)
        assertEquals(result.data.description, reminder.description)
        assertEquals(result.data.location, reminder.location)
        assertEquals(result.data.latitude, reminder.latitude)
        assertEquals(result.data.longitude, reminder.longitude)
    }

    @Test
    fun getReminder_returns_NoData_after_clearing_database() = runBlocking {
        val reminder = ReminderDTO(
            title = "title",
            description = "description",
            location = "Landmark",
            latitude = 25.3,
            longitude = 17.5
        )
        localDataSource.saveReminder(reminder)
        localDataSource.deleteAllReminders()
        // WHEN  - Task retrieved by ID.
        val result = localDataSource.getReminder(reminder.id)
        result as Result.Error
        assertEquals(result.message, "Reminder not found!")
    }

}
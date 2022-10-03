package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun addReminder_then_getReminderByID_returns_expected_value() = runTest {
        // GIVEN
        val reminder = ReminderDTO(
            title = "title",
            description = "description",
            location = "Landmark",
            latitude = 25.3,
            longitude = 17.5
        )
        database.reminderDao().saveReminder(reminder)

        // WHEN
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertNotEquals(null, loaded as ReminderDTO)
        assertEquals(reminder.id, loaded.id)
        assertEquals(reminder.title, loaded.title)
        assertEquals(reminder.location, loaded.location)
        assertEquals(reminder.latitude, loaded.latitude)
        assertEquals(reminder.longitude, loaded.longitude)
    }
}
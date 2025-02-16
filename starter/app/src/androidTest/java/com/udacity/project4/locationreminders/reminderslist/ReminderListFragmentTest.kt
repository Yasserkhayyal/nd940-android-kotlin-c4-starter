package com.udacity.project4.locationreminders.reminderslist

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext.get

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var fragmentScenario: FragmentScenario<ReminderListFragment>
    private val fakeDataSource by get().koin.inject<ReminderDataSource>()

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun setup() {
        runBlocking {
            fakeDataSource.deleteAllReminders()
        }
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun noDataTextView_DisplayedInUi() {
        //Given
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)
        //Then
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        fragmentScenario.close()
    }

    @Test
    fun reminderDataItem_DisplayedInUi() = runTest {
        //Given
        val reminderDTO = ReminderDTO(
            "Reminder title",
            "Reminder Description",
            "Lat: 21.0, Lng:28.0",
            21.0,
            28.0,
            0
        )
        fakeDataSource.saveReminder(reminderDTO)

        //When
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        //Then
        onView(withId(R.id.title)).check(matches(isDisplayed()))
        onView(withId(R.id.title)).check(matches(withText(reminderDTO.title)))
        onView(withId(R.id.description)).check(matches(isDisplayed()))
        onView(withId(R.id.description)).check(matches(withText(reminderDTO.description)))
        onView(withId(R.id.location)).check(matches(isDisplayed()))
        onView(withId(R.id.location)).check(matches(withText(reminderDTO.location)))
        fragmentScenario.close()
    }

    @Test
    fun given_a_reminder_is_added_then_removed_noDataTextView_DisplayedInUi() = runTest {
        //Given
        val reminderDTO = ReminderDTO(
            "Reminder title",
            "Reminder Description",
            "Lat: 21.0, Lng:28.0",
            21.0,
            28.0,
            0
        )
        fakeDataSource.saveReminder(reminderDTO)

        //When
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)
        fakeDataSource.deleteReminder(reminderDTO)
        fragmentScenario.moveToState(Lifecycle.State.STARTED)//to re-trigger loading of data
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)


        //Then
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        fragmentScenario.close()
    }

    @Test
    fun given_a_reminder_is_added_then_updated_then_expected_data_DisplayedInUi() = runTest {
        //Given
        val reminderDTO = ReminderDTO(
            "Reminder title",
            "Reminder Description",
            "Lat: 21.0, Lng:28.0",
            21.0,
            28.0,
            0
        )
        fakeDataSource.saveReminder(reminderDTO)
        fakeDataSource.updateReminder(reminderDTO.copy(title = "testTitle"))

        //When
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        //Then
        onView(withId(R.id.title)).check(matches(isDisplayed()))
        onView(withId(R.id.title)).check(matches(withText("testTitle")))
        fragmentScenario.close()
    }

    @Test
    fun clickOn_addReminderFAB_navigate_to_Save_Reminder() {
        //Given
        val navController = mockk<NavController> {
            every { navigate(any<NavDirections>()) } just runs
        }
        //When
        fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme).onFragment {
                Navigation.setViewNavController(it.view!!, navController)
            }
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        //Then
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify { navController.navigate(ReminderListFragmentDirections.toSaveReminder(null)) }
        fragmentScenario.close()
    }

}
package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext.get

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderFragmentTest {

    private lateinit var fragmentScenario: FragmentScenario<SaveReminderFragment>
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
    fun given_non_null_navArgs_viewModel_fields_contain_expected_values() {
        //Given
        val reminderDataItem = ReminderDataItem(
            title = "title",
            description = "description",
            location = "Landmark",
            latitude = 25.3,
            longitude = 17.5,
            id = "0"
        )
        val bundle = SaveReminderFragmentArgs(reminderDataItem).toBundle()
        fragmentScenario = launchFragmentInContainer(bundle, themeResId = R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.reminderTitle)).check(matches(withText(reminderDataItem.title)))
        onView(withId(R.id.reminderDescription)).check(matches(withText(reminderDataItem.description)))
        onView(withId(R.id.selectedLocationName)).check(matches(withText(reminderDataItem.location)))
        fragmentScenario.close()
    }

    @Test
    fun given_non_null_navArgs_and_new_location_selected_viewModel_fields_contain_expected_values() {
        //Given
        val reminderDataItem = ReminderDataItem(
            title = "title",
            description = "description",
            location = "Landmark",
            latitude = 25.3,
            longitude = 17.5,
            id = "0"
        )
        val bundle = SaveReminderFragmentArgs(reminderDataItem).toBundle()
        fragmentScenario = launchFragmentInContainer(bundle, themeResId = R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)
        fragmentScenario.onFragment {
            it._viewModel.reminderSelectedLocationStr.value = "new Lat: 25.0, Lng: 22.0"
            it._viewModel.latitude.value = 25.0
            it._viewModel.longitude.value = 22.0
        }
        onView(withId(R.id.reminderTitle)).check(matches(withText(reminderDataItem.title)))
        onView(withId(R.id.reminderDescription)).check(matches(withText(reminderDataItem.description)))
        onView(withId(R.id.selectedLocationName)).check(matches(withText("new Lat: 25.0, Lng: 22.0")))
        fragmentScenario.close()
    }

    @Test
    fun given_null_title_reminder_item_data_please_enter_title_snackBar_is_shown() {
        //Given
        fragmentScenario = launchFragmentInContainer<SaveReminderFragment>(
            Bundle(),
            themeResId = R.style.AppTheme
        ).onFragment {
            with(it._viewModel) {
                reminderTitle.value = null
            }
        }
        dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))
        fragmentScenario.close()
    }

    @Test
    fun navigate_to_SelectLocationFragment_when_selectLocationViewGroup_is_clicked() {
        //Given
        val navController = mockk<NavController> {
            every { navigate(any<NavDirections>()) } just runs
        }
        fragmentScenario = launchFragmentInContainer<SaveReminderFragment>(
            Bundle(),
            themeResId = R.style.AppTheme
        ).onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.select_location_view_group)).perform(click())
        verify { navController.navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()) }
        fragmentScenario.close()
    }

}
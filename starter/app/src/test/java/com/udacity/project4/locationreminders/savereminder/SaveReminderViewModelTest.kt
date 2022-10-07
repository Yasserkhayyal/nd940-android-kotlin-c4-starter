package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.S])
class SaveReminderViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var testSubject: SaveReminderViewModel
    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val fakeDataSource = FakeDataSource()
    private val reminderTitleLiveDataObserver = mockk<Observer<String?>>(relaxed = true)
    private val reminderDescriptionLiveDataObserver = mockk<Observer<String?>>(relaxed = true)
    private val reminderSelectedLocationStrLiveDataObserver =
        mockk<Observer<String?>>(relaxed = true)
    private val selectedPOILiveDataObserver = mockk<Observer<PointOfInterest?>>(relaxed = true)
    private val latitudeLiveDataObserver = mockk<Observer<Double?>>(relaxed = true)
    private val longitudeLiveDataObserver = mockk<Observer<Double?>>(relaxed = true)
    private val locationPermissionsRequestedLiveDataObserver =
        mockk<Observer<Boolean>>(relaxed = true)
    private val locationPermissionsGrantedLiveDataObserver =
        mockk<Observer<Boolean>>(relaxed = true)
    private val locationSelectedLiveDataObserver = mockk<Observer<Boolean>>(relaxed = true)
    private val reminderDataItemLiveDataObserver =
        mockk<Observer<ReminderDataItem?>>(relaxed = true)
    private val navigationCommandLiveDataObserver =
        mockk<Observer<NavigationCommand>>(relaxed = true)
    private val loadingLiveDataObserver = mockk<Observer<Boolean>>(relaxed = true)
    private val showSnackBarIntLiveDataObserver = mockk<Observer<Int>>(relaxed = true)
    private val showToastLiveDataObserver = mockk<Observer<String>>(relaxed = true)

    @Before
    fun setup() {
        stopKoin() //stop koin before each test case
        testSubject = SaveReminderViewModel(application, fakeDataSource)
        testSubject.reminderTitle.observeForever(reminderTitleLiveDataObserver)
        testSubject.reminderDescription.observeForever(reminderDescriptionLiveDataObserver)
        testSubject.reminderSelectedLocationStr.observeForever(
            reminderSelectedLocationStrLiveDataObserver
        )
        testSubject.selectedPOI.observeForever(selectedPOILiveDataObserver)
        testSubject.latitude.observeForever(latitudeLiveDataObserver)
        testSubject.longitude.observeForever(longitudeLiveDataObserver)
        testSubject.locationPermissionsRequested.observeForever(
            locationPermissionsRequestedLiveDataObserver
        )
        testSubject.locationPermissionsGranted.observeForever(
            locationPermissionsGrantedLiveDataObserver
        )
        testSubject.locationSelected.observeForever(locationSelectedLiveDataObserver)
        testSubject.reminderDataItem.observeForever(reminderDataItemLiveDataObserver)
        testSubject.navigationCommand.observeForever(navigationCommandLiveDataObserver)
        testSubject.showLoading.observeForever(loadingLiveDataObserver)
        testSubject.showSnackBarInt.observeForever(showSnackBarIntLiveDataObserver)
        testSubject.showToast.observeForever(showToastLiveDataObserver)
        mockkStatic(UUID::randomUUID)
        every { UUID.randomUUID().toString() } returns "0"
    }

    @After
    fun tearDown() {
        testSubject.reminderTitle.removeObserver(reminderTitleLiveDataObserver)
        testSubject.reminderDescription.removeObserver(reminderDescriptionLiveDataObserver)
        testSubject.reminderSelectedLocationStr.removeObserver(
            reminderSelectedLocationStrLiveDataObserver
        )
        testSubject.selectedPOI.removeObserver(selectedPOILiveDataObserver)
        testSubject.latitude.removeObserver(latitudeLiveDataObserver)
        testSubject.longitude.removeObserver(longitudeLiveDataObserver)
        testSubject.locationPermissionsRequested.removeObserver(
            locationPermissionsRequestedLiveDataObserver
        )
        testSubject.locationPermissionsGranted.removeObserver(
            locationPermissionsGrantedLiveDataObserver
        )
        testSubject.locationSelected.removeObserver(locationSelectedLiveDataObserver)
        testSubject.reminderDataItem.removeObserver(reminderDataItemLiveDataObserver)
        testSubject.navigationCommand.removeObserver(navigationCommandLiveDataObserver)
        testSubject.showLoading.removeObserver(loadingLiveDataObserver)
        testSubject.showSnackBarInt.removeObserver(showSnackBarIntLiveDataObserver)
        testSubject.showToast.removeObserver(showToastLiveDataObserver)
    }

    @Test
    fun saveReminder_is_successful_then_liveData_variables_contains_expected_success_values() =
        mainCoroutineRule.scope.runTest {
            //Given
            val reminderDataItem =
                ReminderDataItem("testTitle", "testDescription", "Lat:29.5,Lng:31.2", 29.5, 31.2)
            val fakeReminderDTO =
                ReminderDTO("testTitle", "testDescription", "Lat:29.5,Lng:31.2", 29.5, 31.2, "0")
            val expectedReminderList = mutableListOf(fakeReminderDTO)

            //When
            testSubject.saveReminder(reminderDataItem)

            //Then
            verify(Ordering.SEQUENCE) {
                loadingLiveDataObserver.onChanged(true)
                loadingLiveDataObserver.onChanged(false)
                showToastLiveDataObserver.onChanged(application.getString(R.string.reminder_saved))
                navigationCommandLiveDataObserver.onChanged(NavigationCommand.Back)
            }
            fakeDataSource.reminderList shouldBeEqualTo expectedReminderList
        }

    @Test
    fun validateEnteredData_empty_title_returns_false() = mainCoroutineRule.scope.runTest {
        //Given
        testSubject.isEditingMode = false
        testSubject.reminderTitle.value = ""
        testSubject.reminderDescription.value = "testDescription"
        testSubject.reminderSelectedLocationStr.value = "Lat:29.5,Lng:31.2"
        testSubject.latitude.value = 29.5
        testSubject.longitude.value = 31.2

        //When
        val isDataValid = testSubject.validateEnteredData()

        //Then
        verify(Ordering.SEQUENCE) {
            showSnackBarIntLiveDataObserver.onChanged(R.string.err_enter_title)
        }
        isDataValid shouldBeEqualTo false
    }

    @Test
    fun validateEnteredData_null_location_returns_false() = mainCoroutineRule.scope.runTest {
        //Given
        testSubject.isEditingMode = false
        testSubject.reminderTitle.value = "testTitle"
        testSubject.reminderDescription.value = "testDescription"
        testSubject.reminderSelectedLocationStr.value = null
        testSubject.latitude.value = 29.5
        testSubject.longitude.value = 31.2

        //When
        val isDataValid = testSubject.validateEnteredData()

        //Then
        verify(Ordering.SEQUENCE) {
            showSnackBarIntLiveDataObserver.onChanged(R.string.err_select_location)
        }
        isDataValid shouldBeEqualTo false
    }

    @Test
    fun validateEnteredData_null_latitude_returns_false() = mainCoroutineRule.scope.runTest {
        //Given
        testSubject.isEditingMode = false
        testSubject.reminderTitle.value = "testTitle"
        testSubject.reminderDescription.value = "testDescription"
        testSubject.reminderSelectedLocationStr.value = "Lat:29.5,Lng:31.2"
        testSubject.latitude.value = null
        testSubject.longitude.value = 31.2

        //When
        val isDataValid = testSubject.validateEnteredData()

        //Then
        verify(Ordering.SEQUENCE) {
            showSnackBarIntLiveDataObserver.onChanged(R.string.err_invalid_location)
        }
        isDataValid shouldBeEqualTo false
    }

    @Test
    fun validateEnteredData_null_longitude_returns_false() = mainCoroutineRule.scope.runTest {
        //Given
        testSubject.isEditingMode = false
        testSubject.reminderTitle.value = "testTitle"
        testSubject.reminderDescription.value = "testDescription"
        testSubject.reminderSelectedLocationStr.value = "Lat:29.5,Lng:31.2"
        testSubject.latitude.value = 29.5
        testSubject.longitude.value = null

        //When
        val isDataValid = testSubject.validateEnteredData()

        //Then
        verify(Ordering.SEQUENCE) {
            showSnackBarIntLiveDataObserver.onChanged(R.string.err_invalid_location)
        }
        isDataValid shouldBeEqualTo false
    }

    @Test
    fun validateEnteredData_all_fields_valid_returns_true() = mainCoroutineRule.scope.runTest {
        //Given
        testSubject.isEditingMode = false
        testSubject.reminderTitle.value = "testTitle"
        testSubject.reminderDescription.value = "testDescription"
        testSubject.reminderSelectedLocationStr.value = "Lat:29.5,Lng:31.2"
        testSubject.latitude.value = 29.5
        testSubject.longitude.value = 31.7

        //When
        val isDataValid = testSubject.validateEnteredData()

        //Then
        verify(Ordering.SEQUENCE) {
            reminderDataItemLiveDataObserver.onChanged(
                ReminderDataItem(
                    "testTitle",
                    "testDescription",
                    "Lat:29.5,Lng:31.2",
                    29.5,
                    31.7,
                    "0"
                )
            )
        }
        isDataValid shouldBeEqualTo true
    }

    @Test
    fun validateEnteredData_all_valid_and_isEditing_returns_true() =
        mainCoroutineRule.scope.runTest {
            //Given
            testSubject.reminderDataItem.value = ReminderDataItem(
                "Title",
                "Description",
                "Lat:17.5,Lng:22.1",
                17.5,
                22.1,
            )
            testSubject.isEditingMode = true
            testSubject.reminderTitle.value = "Title"
            testSubject.reminderDescription.value = "Description"
            testSubject.reminderSelectedLocationStr.value = "Lat:29.5,Lng:31.2"
            testSubject.latitude.value = 29.5
            testSubject.longitude.value = 31.7

            //When
            val isDataValid = testSubject.validateEnteredData()

            //Then
            verify(Ordering.SEQUENCE) {
                reminderDataItemLiveDataObserver.onChanged( //called because of setting the initial value in this test case
                    ReminderDataItem(
                        "Title",
                        "Description",
                        "Lat:17.5,Lng:22.1",
                        17.5,
                        22.1,
                        "0"
                    )
                )
                reminderDataItemLiveDataObserver.onChanged(
                    ReminderDataItem(
                        "Title",
                        "Description",
                        "Lat:29.5,Lng:31.2",
                        29.5,
                        31.7,
                        "0"
                    )
                )
            }
            isDataValid shouldBeEqualTo true
        }

    @Test
    fun decomposeReminderItem_non_POI_location_sets_expected_values() =
        mainCoroutineRule.scope.runTest {
            //Given
            val inputReminderDataItem = ReminderDataItem(
                "Title",
                "Description",
                null,
                17.5,
                22.1,
            )

            //When
            testSubject.decomposeReminderItem(inputReminderDataItem)

            //Then
            verify(Ordering.SEQUENCE) {
                reminderTitleLiveDataObserver.onChanged(inputReminderDataItem.title)
                reminderDescriptionLiveDataObserver.onChanged(inputReminderDataItem.description)
                reminderSelectedLocationStrLiveDataObserver.onChanged(
                    String.format(
                        Locale.getDefault(),
                        application.getString(R.string.lat_long_snippet),
                        inputReminderDataItem.latitude,
                        inputReminderDataItem.longitude
                    )
                )
                latitudeLiveDataObserver.onChanged(inputReminderDataItem.latitude)
                longitudeLiveDataObserver.onChanged(inputReminderDataItem.longitude)
                reminderDataItemLiveDataObserver.onChanged(inputReminderDataItem)
                testSubject.isEditingMode shouldBeEqualTo true
            }
        }

    @Test
    fun decomposeReminderItem_POI_location_sets_expected_values() =
        mainCoroutineRule.scope.runTest {
            //Given
            val inputReminderDataItem = ReminderDataItem(
                "Title",
                "Description",
                "Popular Landmark",
                17.5,
                22.1,
            )

            //When
            testSubject.decomposeReminderItem(inputReminderDataItem)

            //Then
            verify(Ordering.SEQUENCE) {
                reminderTitleLiveDataObserver.onChanged(inputReminderDataItem.title)
                reminderDescriptionLiveDataObserver.onChanged(inputReminderDataItem.description)
                reminderSelectedLocationStrLiveDataObserver.onChanged(inputReminderDataItem.location)
                latitudeLiveDataObserver.onChanged(inputReminderDataItem.latitude)
                longitudeLiveDataObserver.onChanged(inputReminderDataItem.longitude)
                reminderDataItemLiveDataObserver.onChanged(inputReminderDataItem)
                testSubject.isEditingMode shouldBeEqualTo true
            }
        }

    @Test
    fun given_saveSelectedLocation_liveData_fields_set_with_expected_values() {
        //Given
        val latLng = LatLng(25.0, 29.0)
        testSubject.saveSelectedLocation(latLng)

        //Then
        verify {
            selectedPOILiveDataObserver.onChanged(null)
            reminderSelectedLocationStrLiveDataObserver.onChanged(
                String.format(
                    Locale.getDefault(),
                    application.getString(R.string.lat_long_snippet),
                    latLng.latitude,
                    latLng.longitude
                )
            )
            latitudeLiveDataObserver.onChanged(latLng.latitude)
            longitudeLiveDataObserver.onChanged(latLng.longitude)
        }
    }

    @Test
    fun given_saveSelectedPOI_liveData_fields_set_with_expected_values() {
        //Given
        val poi = PointOfInterest(LatLng(25.0, 29.0), "10", "Popular Landmark")
        testSubject.saveSelectedPOI(poi)

        //Then
        verify {
            selectedPOILiveDataObserver.onChanged(poi)
            reminderSelectedLocationStrLiveDataObserver.onChanged(poi.name)
            latitudeLiveDataObserver.onChanged(poi.latLng.latitude)
            longitudeLiveDataObserver.onChanged(poi.latLng.longitude)
        }
    }

    @Test
    fun given_onClear_is_called_all_fields_are_reset() {
        //Given
        testSubject.reminderDataItem.value = ReminderDataItem(
            "Title",
            "Description",
            "Lat:17.5,Lng:22.1",
            17.5,
            22.1,
        )
        testSubject.isEditingMode = true
        testSubject.reminderTitle.value = "Title"
        testSubject.reminderDescription.value = "Description"
        testSubject.reminderSelectedLocationStr.value = "Lat:29.5,Lng:31.2"
        testSubject.latitude.value = 29.5
        testSubject.longitude.value = 31.7

        //When
        testSubject.onClear()

        //Then
        verify {
            reminderTitleLiveDataObserver.onChanged(null)
            reminderDescriptionLiveDataObserver.onChanged(null)
            reminderSelectedLocationStrLiveDataObserver.onChanged(null)
            selectedPOILiveDataObserver.onChanged(null)
            latitudeLiveDataObserver.onChanged(null)
            longitudeLiveDataObserver.onChanged(null)
            reminderDataItemLiveDataObserver.onChanged(null)
            testSubject.isEditingMode = false
        }
    }


}
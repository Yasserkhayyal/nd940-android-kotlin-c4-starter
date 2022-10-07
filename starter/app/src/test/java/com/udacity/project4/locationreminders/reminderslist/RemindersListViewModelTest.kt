package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import io.mockk.Ordering
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.S])
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var testSubject: RemindersListViewModel
    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val fakeDataSource = FakeDataSource()
    private val loadingLiveDataObserver = mockk<Observer<Boolean>>(relaxed = true)
    private val reminderListLiveDataObserver =
        mockk<Observer<List<ReminderDataItem>>>(relaxed = true)
    private val showNoDataLiveDataObserver = mockk<Observer<Boolean>>(relaxed = true)
    private val showSnackBarLiveDataObserver = mockk<Observer<String>>(relaxed = true)

    @Before
    fun setup() {
        stopKoin() //stop koin before each test case
        testSubject = RemindersListViewModel(application, fakeDataSource)
        testSubject.showLoading.observeForever(loadingLiveDataObserver)
        testSubject.remindersList.observeForever(reminderListLiveDataObserver)
        testSubject.showNoData.observeForever(showNoDataLiveDataObserver)
        testSubject.showSnackBar.observeForever(showSnackBarLiveDataObserver)
    }

    @After
    fun tearDown() {
        testSubject.showLoading.removeObserver(loadingLiveDataObserver)
        testSubject.remindersList.removeObserver(reminderListLiveDataObserver)
        testSubject.showNoData.removeObserver(showNoDataLiveDataObserver)
        fakeDataSource.reminderList.clear()
        fakeDataSource.shouldShowError = false
    }


    @Test
    fun loadReminders_successful_remindersList_liveData_contains_non_empty_list() =
        mainCoroutineRule.scope.runTest {
            //Given
            val fakeReminderDTO =
                ReminderDTO("testTitle", "testDescription", "Lat:29.5,Lng:31.2", 29.5, 31.2, "0")
            val fakeReminderDataItem = ReminderDataItem(
                fakeReminderDTO.title,
                fakeReminderDTO.description,
                fakeReminderDTO.location,
                fakeReminderDTO.latitude,
                fakeReminderDTO.longitude,
                fakeReminderDTO.id
            )
            fakeDataSource.reminderList.add(fakeReminderDTO)

            //When
            testSubject.loadReminders()

            //Then
            verify(Ordering.SEQUENCE) {
                loadingLiveDataObserver.onChanged(true)
                loadingLiveDataObserver.onChanged(false)
                reminderListLiveDataObserver.onChanged(listOf(fakeReminderDataItem))
                showNoDataLiveDataObserver.onChanged(false)
            }
        }

    @Test
    fun loadReminders_successful_remindersList_liveData_contains_empty_list() =
        mainCoroutineRule.scope.runTest {
            //When
            testSubject.loadReminders()

            //Then
            verify(Ordering.SEQUENCE) {
                loadingLiveDataObserver.onChanged(true)
                loadingLiveDataObserver.onChanged(false)
                reminderListLiveDataObserver.onChanged(listOf())
                showNoDataLiveDataObserver.onChanged(true)
            }
        }

    @Test
    fun loadReminders_failed_remindersList_liveData_has_expected_value() =
        mainCoroutineRule.scope.runTest {
            //Given
            fakeDataSource.shouldShowError = true

            //When
            testSubject.loadReminders()

            //Then
            verify(Ordering.SEQUENCE) {
                loadingLiveDataObserver.onChanged(true)
                loadingLiveDataObserver.onChanged(false)
                showSnackBarLiveDataObserver.onChanged("Test exception")
                showNoDataLiveDataObserver.onChanged(true)
            }
        }
}
package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import java.util.*

class SaveReminderViewModel(
    private val app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {

    val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData<String?>()
    val selectedPOI = MutableLiveData<PointOfInterest?>()
    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()
    val locationPermissionsRequested = SingleLiveEvent<Array<String>>()
    val locationPermissionsGranted = SingleLiveEvent<Boolean>()
    val locationSettingsRequested = SingleLiveEvent<Boolean>()
    val locationSettingsEnabled = SingleLiveEvent<Boolean>()
    val checkLocationSettingsEnabled =
        SingleLiveEvent<Boolean>() //just checking without explicitly requesting
    val locationSelected = SingleLiveEvent<Boolean>()
    val reminderDataItem = MutableLiveData<ReminderDataItem?>()
    var isEditingMode: Boolean = false

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            val result = dataSource.getReminder(reminderData.id)
            if (result is Result.Error) { //reminder doesn't exist, insert it
                val reminderDTO = ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
                dataSource.saveReminder(reminderDTO)
            } else {
                val reminderDTO = ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    (result as Result.Success).data.id
                )
                dataSource.updateReminder(reminderDTO)
            }
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }

    fun deleteReminder(reminderDataItem: ReminderDataItem) {
        val reminderDTO = ReminderDTO(
            title = reminderDataItem.title,
            description = reminderDataItem.description,
            location = reminderDataItem.location,
            latitude = reminderDataItem.latitude,
            longitude = reminderDataItem.longitude,
            id = reminderDataItem.id
        )
        viewModelScope.launch {
            dataSource.deleteReminder(reminderDTO)
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(): Boolean {
        val reminderData = composeReminderDataItem(isEditingMode)

        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }

        if (reminderData.longitude == null || reminderData.latitude == null) {
            showSnackBarInt.value = R.string.err_invalid_location
            return false
        }

        reminderDataItem.value = reminderData //safe to save after validation completed successfully
        return true
    }

    private fun composeReminderDataItem(isEditingMode: Boolean): ReminderDataItem {
        val title = reminderTitle.value
        val description = reminderDescription.value
        val location = reminderSelectedLocationStr.value
        val latitude = latitude.value
        val longitude = longitude.value
        return if (isEditingMode && reminderDataItem.value != null) {
            reminderDataItem.value!!.copy(
                title = title,
                description = description,
                location = location,
                latitude = latitude,
                longitude = longitude
            )
        } else {
            ReminderDataItem(title, description, location, latitude, longitude)
        }
    }

    //in case the ReminderDataItem is passed back from the ReminderListFragment for editing purposes
    fun decomposeReminderItem(reminderData: ReminderDataItem) {
        reminderTitle.value = reminderData.title
        reminderDescription.value = reminderData.description
        reminderSelectedLocationStr.value =
            if (reminderData.location.isNullOrEmpty()) { // to handle non-POI locations selected
                composeLocationFromLatLng(reminderData.latitude!!, reminderData.longitude!!)
            } else {
                reminderData.location
            }
        latitude.value = reminderData.latitude
        longitude.value = reminderData.longitude
        reminderDataItem.value = reminderData
        isEditingMode = true
    }

    fun saveSelectedLocation(latLng: LatLng) {
        selectedPOI.value = null
        reminderSelectedLocationStr.value =
            composeLocationFromLatLng(latLng.latitude, latLng.longitude)
        latitude.value = latLng.latitude
        longitude.value = latLng.longitude
    }

    fun saveSelectedPOI(poi: PointOfInterest) {
        selectedPOI.value = poi
        reminderSelectedLocationStr.value = poi.name
        latitude.value = poi.latLng.latitude
        longitude.value = poi.latLng.longitude
    }

    fun requestLocationPermissions(locationPermissionsRequested: Array<String>) {
        this.locationPermissionsRequested.value = locationPermissionsRequested
    }

    fun requestLocationSettings() {
        locationSettingsRequested.value = true
    }

    fun setLocationPermissionsGranted() {
        locationPermissionsGranted.value = true
    }

    fun setLocationSettingsEnabled() {
        locationSettingsEnabled.value = true
    }

    fun checkLocationSettingsEnabled() {
        checkLocationSettingsEnabled.value = true
    }

    fun clearEditingMode() {
        isEditingMode = false
        locationSelected.value = false
    }

    private fun composeLocationFromLatLng(latitude: Double, longitude: Double) = String.format(
        Locale.getDefault(),
        app.getString(R.string.lat_long_snippet),
        latitude,
        longitude
    )

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
        reminderDataItem.value = null
        isEditingMode = false
    }
}
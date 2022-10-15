package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.ACTION_GEOFENCE_EVENT
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

private const val GEOFENCE_RADIUS_IN_METERS = 100f
private const val TAG = "SaveReminderFragment"

class SaveReminderFragment : BaseFragment() {

    val geofencingClient by inject<GeofencingClient>()

    override val _viewModel by inject<SaveReminderViewModel>()
    private val args: SaveReminderFragmentArgs by navArgs()
    private lateinit var binding: FragmentSaveReminderBinding

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences().
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaveReminderBinding.inflate(layoutInflater).apply {
            viewModel = _viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        setDisplayHomeAsUpEnabled(true)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.selectLocationViewGroup.setOnClickListener {
            //Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
        args.reminderDataItem?.let {
            if (_viewModel.locationSelected.value == true) { //user is navigating back from SelectLocationFragment in the Editing mode
                _viewModel.decomposeReminderItem(
                    it.copy(
                        location = _viewModel.reminderSelectedLocationStr.value,
                        latitude = _viewModel.latitude.value,
                        longitude = _viewModel.longitude.value
                    )
                )
                _viewModel.locationSelected.value = false
            } else { //user is navigating from ReminderListFragment
                _viewModel.decomposeReminderItem(it)
            }
        }

        _viewModel.locationPermissionsGranted.observe(viewLifecycleOwner) {
            if (it && _viewModel.reminderDataItem.value != null) {
                _viewModel.clearEditingMode()
                createGeofence(_viewModel.reminderDataItem.value!!)
            } else {
                _viewModel.showSnackBarInt.value = R.string.invalid_geofence_data
            }
        }

        binding.saveReminder.setOnClickListener {
            if (_viewModel.validateEnteredData()) {
                val requestedPermissions = if (_viewModel.runningQOrLater) {
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                } else {
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                _viewModel.requestLocationPermissions(requestedPermissions)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun createGeofence(reminderDataItem: ReminderDataItem) {
        // Build the Geofence Object
        val geofence = Geofence.Builder()
            // Set the request ID, string to identify the geofence.
            .setRequestId(reminderDataItem.id)
            // Set the circular region of this geofence.
            .setCircularRegion(
                reminderDataItem.latitude!!,
                reminderDataItem.longitude!!,
                GEOFENCE_RADIUS_IN_METERS
            ).setExpirationDuration(TimeUnit.DAYS.toMillis(30))
            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            // Add the geofences to be monitored by geofencing service.
            .addGeofence(geofence)
            .build()

        // Add the new geofence request with the new geofence
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                // Geofences added.
                Log.e("Add Geofence", geofence.requestId)
                _viewModel.saveReminder(reminderDataItem)
            }
            addOnFailureListener {
                _viewModel.showSnackBarInt.value = R.string.geofences_not_added
                if (it.message != null) {
                    Log.w(TAG, it.message!!)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}

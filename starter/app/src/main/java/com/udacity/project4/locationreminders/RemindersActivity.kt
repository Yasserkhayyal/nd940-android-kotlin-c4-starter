package com.udacity.project4.locationreminders

import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseActivity
import com.udacity.project4.databinding.ActivityRemindersBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.android.inject

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : BaseActivity() {
    private val tag = RemindersActivity::class.java.simpleName

    val viewModel by inject<SaveReminderViewModel>()

    private lateinit var binding: ActivityRemindersBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val appPermissionSettingsActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (checkPermissionsGrantedFromAppSettings(viewModel.locationPermissionsRequested.value)) {
                checkDeviceLocationSettings()
            } else {
                viewModel.locationPermissionsRequested.value?.let { permissions ->
                    requestLocationPermissions(permissions)
                }
            }
        }
    private val locationPermissionActivityLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
            if (checkPermissionsGranted(
                    grantResults,
                    viewModel.locationPermissionsRequested.value
                )
            ) {
                checkDeviceLocationSettings()
            } else {
                // Permission denied.
                Snackbar.make(
                    activityBaseBinding.coordinatorLayout,
                    R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.settings) {
                    // Displays App settings screen.
                    appPermissionSettingsActivityLauncher.launch(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    })
                }.show()
            }
        }

    private val locationTaskResolutionActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            it.data
            checkDeviceLocationSettings(resolve = false)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setLayoutContainerContent(R.layout.activity_reminders) as ActivityRemindersBinding
        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).findNavController()
        appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        viewModel.locationPermissionsRequested.observe(this) {
            if (!it.isNullOrEmpty()) {
                requestLocationPermissions(it)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp(appBarConfiguration)

    override fun onDestroy() {
        super.onDestroy()
        appPermissionSettingsActivityLauncher.unregister()
        locationPermissionActivityLauncher.unregister()
        locationTaskResolutionActivityLauncher.unregister()
    }

    @TargetApi(29)
    private fun checkPermissionsGrantedFromAppSettings(permissionsRequested: Array<String>?): Boolean {
        permissionsRequested?.onEach {
            if (PackageManager.PERMISSION_DENIED ==
                ActivityCompat.checkSelfPermission(
                    this,
                    it
                )
            ) return false
        }
        return true
    }

    @TargetApi(29)
    private fun requestLocationPermissions(permissions: Array<String>) {
        locationPermissionActivityLauncher.launch(permissions)
    }

    /*
     *  Uses the Location Client to check the current state of location settings, and gives the user
     *  the opportunity to turn on location services within our app.
     */
    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.status.resolution?.intentSender?.let {
                        locationTaskResolutionActivityLauncher.launch(
                            IntentSenderRequest.Builder(it).build()
                        )
                    }
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(tag, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                showSnackBarRequestingAccessToUserLocation()
            }
        }
        locationSettingsResponseTask.addOnCanceledListener {
            showSnackBarRequestingAccessToUserLocation()
        }
        locationSettingsResponseTask.addOnSuccessListener {
            if (checkPermissionsGrantedFromAppSettings(viewModel.locationPermissionsRequested.value)) {
                viewModel.setLocationPermissionsGranted()
            }
        }
    }

    private fun showSnackBarRequestingAccessToUserLocation() =
        Snackbar.make(
            activityBaseBinding.coordinatorLayout,
            R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
        ).setAction(android.R.string.ok) {
            checkDeviceLocationSettings()
        }.show()

    private fun checkPermissionsGranted(
        grantResults: Map<String, Boolean>,
        permissionsRequested: Array<String>?
    ): Boolean {
        if (permissionsRequested.isNullOrEmpty()) return false
        permissionsRequested.onEach {
            if (grantResults.isEmpty() || grantResults[it] == false) return false
        }
        return true
    }

}

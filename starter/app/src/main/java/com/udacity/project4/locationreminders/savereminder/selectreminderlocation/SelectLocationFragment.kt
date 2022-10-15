package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.gms.tasks.Task
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    override val _viewModel by inject<SaveReminderViewModel>()
    private val mTag = SelectLocationFragment::class.java.simpleName
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentSelectLocationBinding.inflate(layoutInflater).apply {
            viewModel = _viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        setDisplayHomeAsUpEnabled(true)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        _viewModel.locationSelected.observe(viewLifecycleOwner) {
            if (it) {
                lifecycleScope.launch {
                    delay(2000)
                    _viewModel.navigationCommand.value = NavigationCommand.Back
                }
            }
        }
        _viewModel.locationPermissionsGranted.observe(viewLifecycleOwner) {
            if (it) {
                enableMyLocation()
            }
        }
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.map_options, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.normal_map -> {
                    mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    true
                }
                R.id.hybrid_map -> {
                    mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                    true
                }
                R.id.satellite_map -> {
                    mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    true
                }
                R.id.terrain_map -> {
                    mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    true
                }
                android.R.id.home -> {
                    _viewModel.navigationCommand.value = NavigationCommand.Back
                    true
                }
                else -> false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        setMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)
        _viewModel.requestLocationPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    private fun onLocationSelected(latLng: LatLng) {
        _viewModel.saveSelectedLocation(latLng)
        _viewModel.locationSelected.value = true
    }

    private fun onLocationSelected(poi: PointOfInterest) {
        _viewModel.saveSelectedPOI(poi)
        _viewModel.locationSelected.value = true
    }

    private fun setMapStyle(map: GoogleMap) {
        kotlin.runCatching {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
                .let { successful ->
                    if (!successful) {
                        Log.e(mTag, "Style parsing failed.")

                    }
                }
        }.onFailure {
            if (it is Resources.NotFoundException) {
                Log.e(mTag, "Can't find style. Error: ", it)
            }
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener {
            val snippet = String.format(
                Locale.getDefault(),
                getString(R.string.lat_long_snippet),
                it.latitude,
                it.longitude
            )
            map.addMarker(
                MarkerOptions().position(it)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )
            onLocationSelected(it)
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
            onLocationSelected(poi)
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (::mMap.isInitialized) {
            mMap.isMyLocationEnabled = true
            zoomToDeviceLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun zoomToDeviceLocation() {
        val locationResult: Task<Location> =
            LocationServices.getFusedLocationProviderClient(requireActivity()).lastLocation
        locationResult.addOnSuccessListener { location ->
            location?.let {
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            it.latitude,
                            it.longitude
                        ), 15f
                    )
                )
            }
        }
    }

}

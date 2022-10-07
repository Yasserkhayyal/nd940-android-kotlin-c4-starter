package com.udacity.project4.work

import androidx.work.DelegatingWorkerFactory
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.geofence.GeofenceTransitionsWorkerFactory

class IoschedWorkerFactory(
    private val remindersLocalRepository: ReminderDataSource
) : DelegatingWorkerFactory() {
    init {
        addFactory(GeofenceTransitionsWorkerFactory(remindersLocalRepository))
    }
}
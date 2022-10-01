package com.udacity.project4.locationreminders.geofence

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.udacity.project4.locationreminders.data.ReminderDataSource

class GeofenceTransitionsWorkerFactory(
    private val remindersLocalRepository: ReminderDataSource
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {

        return when (workerClassName) {
            GeofenceTransitionsWorker::class.java.name ->
                GeofenceTransitionsWorker(appContext, workerParameters, remindersLocalRepository)
            else ->
                // Return null, so that the base class can delegate to the default WorkerFactory.
                null
        }
    }
}
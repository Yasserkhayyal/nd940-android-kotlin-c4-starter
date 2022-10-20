package com.udacity.project4.locationreminders.geofence

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification


class GeofenceTransitionsWorker(
    private val appContext: Context,
    params: WorkerParameters,
    private val remindersLocalRepository: ReminderDataSource
) : CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "GeofenceTransitionsWorker"
        const val REQUEST_ID_KEY = "RequestId"
    }

    private val tag = GeofenceTransitionsWorker::class.java.simpleName

    @SuppressLint("RestrictedApi")
    override suspend fun doWork(): Result {
        return try {
            inputData.getString(REQUEST_ID_KEY)?.let { fenceId ->
                val result = remindersLocalRepository.getReminder(fenceId.toInt())
                if (result is com.udacity.project4.locationreminders.data.dto.Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        appContext, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
                Result.Success()
            } ?: failWithUnknownRequestId()
        } catch (e: Exception) {
            Log.e(tag, e.message ?: "unknown error occurred")
            Result.failure()
        }
    }

    private fun failWithUnknownRequestId(): Result {
        Log.e(tag, "fenceId is null")
        return Result.failure()
    }
}
package com.udacity.project4

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.google.android.gms.location.GeofencingClient
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.work.IoschedWorkerFactory
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

inline fun <reified T : Application> appModule(app: T) = module {
    //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
    viewModel {
        RemindersListViewModel(
            get(),
            get() as ReminderDataSource
        )
    }
    //Declare singleton definitions to be later injected using by inject()
    single {
        //This view model is declared singleton to be used across multiple fragments
        SaveReminderViewModel(
            get(),
            get() as ReminderDataSource
        )
    }
    single { GeofencingClient(androidContext()) }
    single { LocalDB.createRemindersDao(app) }
    single { IoschedWorkerFactory(get()) }
    single {
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(get(IoschedWorkerFactory::class.java))
            .build()
    }
}


package com.udacity.project4.locationreminders.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.room.Room
import androidx.work.Configuration
import com.google.android.gms.location.GeofencingClient
import com.udacity.project4.TestApplication
import com.udacity.project4.di.ViewModelKey
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.work.IoschedWorkerFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
abstract class TestAppModule {

    companion object {
        @JvmStatic
        @Singleton
        @Provides
        fun provideDataBase(context: Context): RemindersDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                RemindersDatabase::class.java, "locationReminders.db"
            ).build()


        @JvmStatic
        @Provides
        fun provideDao(remindersDatabase: RemindersDatabase): RemindersDao =
            remindersDatabase.reminderDao()

        @JvmStatic
        @Singleton
        @Provides
        fun provideGeofencingClient(context: Context): GeofencingClient = GeofencingClient(context)

        @JvmStatic
        @Provides
        fun provideIoDispatcher() = Dispatchers.IO

        @Singleton
        @Provides
        fun provideWorkManagerConfiguration(
            ioschedWorkerFactory: IoschedWorkerFactory
        ): Configuration {
            return Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .setWorkerFactory(ioschedWorkerFactory)
                .build()
        }
    }

    //added here since it is going to be injected in multiple fragments
    @Binds
    @IntoMap
    @ViewModelKey(SaveReminderViewModel::class)
    abstract fun bindViewModel(viewModel: SaveReminderViewModel): ViewModel

    @Binds
    abstract fun provideReminderDataSource(fakeDataSource: FakeDataSource): ReminderDataSource

    @Binds
    abstract fun provideApplication(testApp: TestApplication): Application

    @Binds
    abstract fun provideContext(testApp: TestApplication): Context
}
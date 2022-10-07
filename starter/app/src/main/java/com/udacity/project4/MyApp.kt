package com.udacity.project4

import android.app.Application
import androidx.work.Configuration
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

open class MyApp : Application(), Configuration.Provider {

    private val workerConfiguration by inject<Configuration>()

    // Setup custom configuration for WorkManager with a DelegatingWorkerFactory
    override fun getWorkManagerConfiguration(): Configuration {
        return workerConfiguration
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(listOf(getMyAppModule(), getDataSourceModule()))
        }
    }

    protected open fun getMyAppModule() = appModule(this)
    protected open fun getDataSourceModule() = module {
        single { RemindersLocalRepository(get()) as ReminderDataSource}
    }
}
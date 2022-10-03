package com.udacity.project4

import android.app.Application
import androidx.work.Configuration
import com.udacity.project4.di.AppComponent
import com.udacity.project4.di.DaggerAppComponent
import javax.inject.Inject

open class MyApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerConfiguration: Configuration

    // Instance of the AppComponent that will be used by all the Activities in the project
    val appComponent: AppComponent by lazy {
        initializeComponent()
    }

    open fun initializeComponent(): AppComponent {
        // Creates an instance of AppComponent using its Factory constructor
        // We pass the applicationContext that will be used as Context in the graph
        return DaggerAppComponent.factory().create(this)
    }

    // Setup custom configuration for WorkManager with a DelegatingWorkerFactory
    override fun getWorkManagerConfiguration(): Configuration {
        return workerConfiguration
    }

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)
    }

}
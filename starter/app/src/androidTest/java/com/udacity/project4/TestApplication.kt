package com.udacity.project4

import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

class TestApplication : MyApp() {

    override fun onCreate() {
        startKoin {
            androidContext(this@TestApplication)
            modules(listOf(getMyAppModule(), getDataSourceModule()))
        }
    }

    override fun getMyAppModule(): Module {
        return appModule(this)
    }

    override fun getDataSourceModule() = module {
        single { FakeDataSource() as ReminderDataSource}
    }
}
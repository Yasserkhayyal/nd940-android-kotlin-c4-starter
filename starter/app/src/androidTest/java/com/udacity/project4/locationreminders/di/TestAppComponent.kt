package com.udacity.project4.locationreminders.di

import com.udacity.project4.TestApplication
import com.udacity.project4.di.AppComponent
import com.udacity.project4.di.SubcomponentsModule
import com.udacity.project4.di.ViewModelBuilderModule
import com.udacity.project4.locationreminders.data.FakeDataSource
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [TestAppModule::class, ViewModelBuilderModule::class, SubcomponentsModule::class])
interface TestAppComponent : AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance testApp: TestApplication): TestAppComponent
    }

    fun inject(testApp: TestApplication)
    fun getFakeDataSource(): FakeDataSource //get the injected instance into graph
}
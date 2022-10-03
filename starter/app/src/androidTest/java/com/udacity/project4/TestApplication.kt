package com.udacity.project4

import com.udacity.project4.di.AppComponent
import com.udacity.project4.locationreminders.di.DaggerTestAppComponent

class TestApplication : MyApp() {

    override fun initializeComponent(): AppComponent {
        // Creates a new TestAppComponent that injects fakes types
        return DaggerTestAppComponent.factory().create(this)
    }
}
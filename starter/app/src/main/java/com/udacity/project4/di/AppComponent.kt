package com.udacity.project4.di

import com.udacity.project4.MyApp
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.reminderslist.di.ReminderListComponent
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        ViewModelBuilderModule::class,
        SubcomponentsModule::class
    ]
)
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance myApp: MyApp): AppComponent
    }

    fun addReminderListComponent(): ReminderListComponent.Factory

    fun inject(remindersActivity: RemindersActivity)
    fun inject(saveReminderFragment: SaveReminderFragment)
    fun inject(selectLocationFragment: SelectLocationFragment)
    fun inject(myApp: MyApp)
}
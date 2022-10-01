package com.udacity.project4.locationreminders.reminderslist.di

import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import dagger.Subcomponent

@Subcomponent(
    modules = [ReminderListModule::class]
)
interface ReminderListComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): ReminderListComponent
    }

    fun inject(fragment: ReminderListFragment)
}
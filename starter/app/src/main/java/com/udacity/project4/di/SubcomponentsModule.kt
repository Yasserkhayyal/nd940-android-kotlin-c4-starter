package com.udacity.project4.di

import com.udacity.project4.locationreminders.reminderslist.di.ReminderListComponent
import dagger.Module

@Module(
    subcomponents = [
        ReminderListComponent::class
    ]
)
class SubcomponentsModule
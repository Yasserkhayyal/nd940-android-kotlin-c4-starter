package com.udacity.project4.locationreminders.reminderslist.di

import androidx.lifecycle.ViewModel
import com.udacity.project4.di.ViewModelKey
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ReminderListModule {

    @Binds
    @IntoMap
    @ViewModelKey(RemindersListViewModel::class)
    abstract fun bindViewModel(viewModel: RemindersListViewModel): ViewModel
}
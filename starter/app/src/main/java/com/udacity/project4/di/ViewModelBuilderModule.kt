package com.udacity.project4.di

import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelBuilderModule {

    @Binds
    abstract fun bindViewModelFactory(
        factory: LocationRemindersViewModelFactory
    ): ViewModelProvider.Factory
}
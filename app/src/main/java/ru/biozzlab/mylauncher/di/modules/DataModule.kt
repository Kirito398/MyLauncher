package ru.biozzlab.mylauncher.di.modules

import dagger.Module
import dagger.Provides
import ru.biozzlab.mylauncher.data.Cache
import ru.biozzlab.mylauncher.data.RepositoryImpl
import ru.biozzlab.mylauncher.domain.interfaces.Repository
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    @Singleton
    fun provideRepository(cache: Cache): Repository = RepositoryImpl(cache)
}
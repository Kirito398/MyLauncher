package ru.biozzlab.mylauncher.di.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import ru.biozzlab.mylauncher.cache.CacheImpl
import ru.biozzlab.mylauncher.cache.RoomManager
import ru.biozzlab.mylauncher.cache.SharedPrefsManager
import ru.biozzlab.mylauncher.data.Cache

@Module
class CacheModule(val context: Context) {

    @Provides
    fun provideApplication(): Application = context as Application

    @Provides
    fun provideCache(roomManager: RoomManager, prefsManager: SharedPrefsManager): Cache = CacheImpl(roomManager, prefsManager)

    @Provides
    fun provideRoomManager(): RoomManager = RoomManager.getClient(context)

    @Provides
    fun provideSharedPreferences(): SharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
}
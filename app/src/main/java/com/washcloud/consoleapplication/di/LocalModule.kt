package com.washcloud.consoleapplication.di

import android.content.Context
import com.washcloud.consoleapplication.local.preferences.IPrefsManager
import com.washcloud.consoleapplication.local.preferences.PrefsManager
import com.washcloud.consoleapplication.remote.config.JsonMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {
    @Provides
    @Singleton
    fun providePrefsManager(@ApplicationContext context: Context, jsonMapper: JsonMapper): IPrefsManager {
        return PrefsManager(jsonMapper, context)
    }
}
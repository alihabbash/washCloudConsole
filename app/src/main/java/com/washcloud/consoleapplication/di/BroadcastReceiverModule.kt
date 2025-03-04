package com.washcloud.consoleapplication.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import com.washcloud.consoleapplication.repository.BroadcastReceiverRepository

@Module
@InstallIn(ViewModelComponent::class)
object BroadcastReceiverModule {

    @Provides
    fun provideBroadcastReceiverRepository(@ApplicationContext context: Context): BroadcastReceiverRepository {
        return BroadcastReceiverRepository(context)
    }
}
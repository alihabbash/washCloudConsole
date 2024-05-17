package com.washcloud.consoleapplication.di

import android.content.Context
import androidx.room.Room
import com.washcloud.consoleapplication.local.database.ConsoleDatabase
import com.washcloud.consoleapplication.local.database.DatabaseConstants
import com.washcloud.consoleapplication.local.database.dao.PickupDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideConsoleDatabase(@ApplicationContext context: Context): ConsoleDatabase {
        return Room.databaseBuilder(
            context,
            ConsoleDatabase::class.java,
            DatabaseConstants.DATABASE_NAME
        )
            .build()
    }

    @Provides
    @Singleton
    fun providePickupDao(database: ConsoleDatabase): PickupDao {
        return database.getPickupDao()
    }
}
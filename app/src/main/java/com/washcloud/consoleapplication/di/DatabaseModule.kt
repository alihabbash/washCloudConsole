package com.washcloud.consoleapplication.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.washcloud.consoleapplication.local.database.ConsoleDatabase
import com.washcloud.consoleapplication.local.database.DatabaseConstants
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
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
    fun provideBoxDao(database: ConsoleDatabase): BoxDao {
        return database.getBoxDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: ConsoleDatabase): TransactionDao {
        return database.getTransactionDao()
    }
}
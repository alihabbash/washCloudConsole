package com.washcloud.consoleapplication.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.dto.TransactionDto
import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase



@Database(

    entities = [TransactionDto::class, BoxDto::class],
    version = DatabaseConstants.DATABASE_VERSION,

)
@TypeConverters(Converters::class)
abstract class ConsoleDatabase : RoomDatabase(){
    abstract fun getTransactionDao(): TransactionDao

    abstract fun getBoxDao(): BoxDao


}
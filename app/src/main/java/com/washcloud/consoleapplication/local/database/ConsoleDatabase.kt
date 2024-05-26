package com.washcloud.consoleapplication.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.dto.TransactionDto

@Database(
    version = DatabaseConstants.DATABASE_VERSION,
    entities = [TransactionDto::class, BoxDto::class]
)
@TypeConverters(Converters::class)
abstract class ConsoleDatabase : RoomDatabase(){
    abstract fun getTransactionDao(): TransactionDao

    abstract fun getBoxDao(): BoxDao

}
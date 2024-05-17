package com.washcloud.consoleapplication.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.washcloud.consoleapplication.local.database.dao.PickupDao
import com.washcloud.consoleapplication.local.database.dto.PickupDto

@Database(
    version = DatabaseConstants.DATABASE_VERSION,
    entities = [PickupDto::class]
)
@TypeConverters(Converters::class)
abstract class ConsoleDatabase : RoomDatabase(){
    abstract fun getPickupDao(): PickupDao
}
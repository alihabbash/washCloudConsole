package com.washcloud.consoleapplication.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.washcloud.consoleapplication.local.database.dto.PickupDto

@Dao
interface PickupDao {
    @Query("SELECT * FROM pickup")
    suspend fun getAllPickups(): List<PickupDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPickup(pickupDto: PickupDto)

    @Query("DELETE FROM pickup WHERE id = :id")
    suspend fun deletePickup(id: Long)

    @Query("SELECT * FROM pickup WHERE id = :id")
    suspend fun getPickup(id: Long): PickupDto

    @Query("DELETE FROM pickup")
    suspend fun deleteAllPickups()

}
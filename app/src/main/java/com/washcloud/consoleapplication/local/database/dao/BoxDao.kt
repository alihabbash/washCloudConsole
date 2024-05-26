package com.washcloud.consoleapplication.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.washcloud.consoleapplication.local.database.dto.BoxDto

@Dao
interface BoxDao {
    @Query("SELECT * FROM boxes")
    suspend fun getAllBoxes(): List<BoxDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBox(box: BoxDto)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteBox(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getBox(id: Long): BoxDto

    @Query("DELETE FROM transactions")
    suspend fun deleteAllBoxes()
}
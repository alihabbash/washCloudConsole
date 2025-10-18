package com.washcloud.consoleapplication.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.utils.BoxState

@Dao
interface BoxDao {
    @Query("SELECT * FROM boxes")
    suspend fun getAllBoxes(): List<BoxDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBox(box: BoxDto)

    @Query("DELETE FROM boxes WHERE box_id = :id AND box_type = :boxType")
    suspend fun deleteBoxByIdAndType(id: Long, boxType: String)

    @Query("SELECT * FROM boxes WHERE id = :id")
    suspend fun getBox(id: Long): BoxDto

    @Query("DELETE FROM boxes")
    suspend fun deleteAllBoxes()

    @Update
    suspend fun updateBox(box: BoxDto)

    @Query("UPDATE boxes SET box_state = :newState WHERE box_id = :boxId")
    suspend fun updateBoxState(boxId: Long, newState: BoxState)

    @Query("SELECT * FROM boxes WHERE box_id = :boxId AND box_type = :boxType LIMIT 1")
    suspend fun getBoxById(boxId: Long, boxType: String): BoxDto?



}
package com.washcloud.consoleapplication.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.washcloud.consoleapplication.local.database.dto.TransactionDto

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<TransactionDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(pickupDto: TransactionDto)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransaction(id: Long): TransactionDto

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM transactions WHERE order_serial = :orderSerial")
    suspend fun deleteTransactionsByOrderSerial(orderSerial: String)
}
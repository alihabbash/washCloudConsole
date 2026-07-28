package com.washcloud.consoleapplication.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.washcloud.consoleapplication.local.database.dto.CustomerUserDto

@Dao
interface CustomerUserDao {
    @Query("SELECT * FROM customer_users WHERE user_id = :userId")
    suspend fun getCustomerUser(userId: Long): CustomerUserDto?

    @Query("SELECT * FROM customer_users")
    suspend fun getAllCustomerUsers(): List<CustomerUserDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerUser(customerUser: CustomerUserDto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerUsers(customerUsers: List<CustomerUserDto>)

    @Update
    suspend fun updateCustomerUser(customerUser: CustomerUserDto)

    @Query("DELETE FROM customer_users WHERE user_id = :userId")
    suspend fun deleteCustomerUser(userId: Long)

    @Query("DELETE FROM customer_users")
    suspend fun deleteAllCustomerUsers()
}

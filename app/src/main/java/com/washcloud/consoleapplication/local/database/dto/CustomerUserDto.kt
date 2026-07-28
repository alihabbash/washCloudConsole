package com.washcloud.consoleapplication.local.database.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_users")
data class CustomerUserDto(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "customer_name") val customerName: String?,
    @ColumnInfo(name = "phone_number") val phoneNumber: String?,
    @ColumnInfo(name = "console_password") val consolePassword: String?,
    @ColumnInfo(name = "console_last_update") val consoleLastUpdate: String
)

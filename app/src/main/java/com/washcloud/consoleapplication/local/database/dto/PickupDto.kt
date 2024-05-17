package com.washcloud.consoleapplication.local.database.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pickup")
data class PickupDto(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "order_serial") val orderSerial: String,
    @ColumnInfo(name = "box") val box: String,
){
    constructor(
        orderSerial: String,
        box: String,
    ): this(0, orderSerial, box )
}

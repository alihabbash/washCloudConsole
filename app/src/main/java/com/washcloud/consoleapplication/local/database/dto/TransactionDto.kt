package com.washcloud.consoleapplication.local.database.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.washcloud.consoleapplication.local.database.utils.BoxSizeType
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionDto(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "order_serial") val orderSerial: String,
    @ColumnInfo(name = "order_id") val orderId: Long,
    @ColumnInfo(name = "box_id") val boxId: Long,//or conveyor id
    @ColumnInfo(name = "trans_date") val trnasDate: Date,
    @ColumnInfo(name = "branch_id") val branchId: Long,
    @ColumnInfo(name = "trans_type") val trnasType: TransactionType,//drop off - pickup
    @ColumnInfo(name = "box_Size") val boxSize: BoxSizeType,
){
    constructor(
        orderSerial: String,
        orderId: Long,
        boxId: Long,
        trnasDate: Date,
        branchId: Long,
        trnasType: TransactionType,
        boxSize: BoxSizeType,
    ): this(0, orderSerial, orderId, boxId, trnasDate, branchId,
        trnasType, boxSize)
}


//todo box but table name
// id - number - type (box - conveyor) - order_serial - status(drop off - pickup)
// state(Available - Occupied - Blocked) - order_id - branch_id - box_size_id
package com.washcloud.consoleapplication.local.database.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.washcloud.consoleapplication.local.database.utils.BoxSizeType
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.local.database.utils.BoxType
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import java.util.Date

@Entity(tableName = "boxes")
data class BoxDto(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "order_serial") val orderSerial: String,
    @ColumnInfo(name = "order_id") val orderId: Long,
    @ColumnInfo(name = "box_id") val boxId: Long,//or conveyor id
    @ColumnInfo(name = "trans_date") val trnasDate: Date,
    @ColumnInfo(name = "branch_id") val branchId: Long,
    @ColumnInfo(name = "trans_type") val trnasType: TransactionType,//drop off - pickup
    @ColumnInfo(name = "box_Size") val boxSize: BoxSizeType,
    @ColumnInfo(name = "box_type") val boxType: BoxType,
    @ColumnInfo(name = "box_state") val boxState: BoxState,
){
    constructor(
        orderSerial: String,
        orderId: Long,
        boxId: Long,
        trnasDate: Date,
        branchId: Long,
        trnasType: TransactionType,
        boxSize: BoxSizeType,
        boxType: BoxType,
        boxState: BoxState,
    ): this(0, orderSerial, orderId, boxId, trnasDate, branchId,
        trnasType, boxSize, boxType, boxState)
}

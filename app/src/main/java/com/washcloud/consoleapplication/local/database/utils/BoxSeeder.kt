package com.washcloud.consoleapplication.local.database.utils



import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.utils.BoxSizeType
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.local.database.utils.BoxType
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import java.util.Date



object BoxSeeder {
    suspend fun seed(boxDao: BoxDao) {
        val existingBoxes = boxDao.getAllBoxes()
        if (existingBoxes.isEmpty()) {
            val boxes = List(9) {
                BoxDto(
                    orderSerial = "order_serial_$it",
                    orderId = it.toLong(),
                    boxId =  it.toLong() + 1,
                    boxNumber =  it.toLong() + 2,
                    trnasDate = Date(),
                    branchId = 1L,
                    trnasType = TransactionType.DROP_OFF,
                    boxSize = BoxSizeType.MEDIUM,
                    boxType = BoxType.BOX,
                    boxState = BoxState.AVAILABLE,
                    stationId = 2L,
                    portId = "port_$it"
                )
            }

            boxes.forEach { box ->
                boxDao.insertBox(box)
            }
        }else{
            println("DB inserted: "  + existingBoxes.last())
        }
    }
}
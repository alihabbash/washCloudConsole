package com.washcloud.consoleapplication.hardware

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.washcloud.consoleapplication.hardware.CRC16Modbus.compute
import com.washcloud.consoleapplication.hardware.CRC16Modbus.hexStringToByteArray
import com.washcloud.consoleapplication.hardware.CRC16Modbus.toHex
import com.washcloud.consoleapplication.utils.FileLogger
import tp.xmaihh.serialport.SerialHelper
import tp.xmaihh.serialport.bean.ComBean
import tp.xmaihh.serialport.utils.ByteUtil
import java.io.IOException



class SerialPortService : Service() {
    private lateinit var serialHelper: SerialHelper
    private lateinit var serialHelperConveyor: SerialHelper
    private  lateinit var serialHelperConveyorDoor: SerialHelper
    private  var position: Int = 0
    private  val holeNumber: Int = 201
    private val buffer = StringBuilder()

    private val dataReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.washcloud.open_door") {
                val stationId = intent.getStringExtra("stationId")
                val boxId = intent.getStringExtra("boxId")
                openBox(boxId!!, stationId!!)
                FileLogger.log(context, "SerialPortService", "Opening box $boxId at station $stationId")
            } else if (intent.action == "com.washcloud.check_door") {
                val stationId = intent.getStringExtra("stationId")
                val boxId = intent.getStringExtra("boxId")
                checkBox(boxId!!, stationId!!)
            }else if (intent.action == "com.washcloud.conveyor_open") {

                FileLogger.log(context, "SerialPortService", "Opening conveyor requested" )
                val conveyorNumber = intent.getStringExtra("conveyorNumber")
                openConveyor(conveyorNumber!!.toInt());

            }else if (intent.action == "com.washcloud.conveyor_close") {
                FileLogger.log(context, "SerialPortService", "close door conveyor requested" )
                closeConveyorDoor()
            } else if (intent.action == "com.washcloud.conveyor_open_door") {
                FileLogger.log(context, "SerialPortService", "open door conveyor requested" )
                 openConveyorDoor()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter()
        filter.addAction("com.washcloud.open_door")
        filter.addAction("com.washcloud.check_door")
        filter.addAction("com.washcloud.conveyor_open")
        filter.addAction("com.washcloud.conveyor_close")
        filter.addAction("com.washcloud.conveyor_open_door")

        registerReceiver(dataReceiver, filter)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        serialHelper = object : SerialHelper("dev/ttyS1", 9600) {
            public override fun onDataReceived(comBean: ComBean) {

                val dataReceive = ByteUtil.ByteArrToHex(comBean.bRec)

                FileLogger.log(
                    applicationContext,
                    "SerialPortService",
                    "Received data (hex): $dataReceive"
                )
                //  FileLogger.log(applicationContext, "SerialPortService", "Received data (raw bytes): ${comBean.bRec.contentToString()}")
                val broadcastIntent = Intent("com.washcloud.door_status")
                if (dataReceive.startsWith("900785")) {
                    FileLogger.log(
                        applicationContext,
                        "SerialPortService 900785",
                        "Received data: $dataReceive"
                    )
                    broadcastIntent.putExtra("stationId", dataReceive.substring(6, 8))
                    broadcastIntent.putExtra("boxId", dataReceive.substring(8, 10))
                    val isOpen = dataReceive.substring(10, 12) == "01"
                    broadcastIntent.putExtra("status", isOpen)
                    broadcastIntent.putExtra("data", dataReceive)
                    sendBroadcast(broadcastIntent)
                } else if (dataReceive.startsWith("900792")) {
                    FileLogger.log(
                        applicationContext,
                        "SerialPortService 900792",
                        "Received data: $dataReceive"
                    )
                    broadcastIntent.putExtra("stationId", dataReceive.substring(6, 8))
                    broadcastIntent.putExtra("boxId", dataReceive.substring(10, 12))
                    val isOpen = dataReceive.substring(8, 10) == "01"
                    broadcastIntent.putExtra("status", isOpen)
                    broadcastIntent.putExtra("data", dataReceive)
                    sendBroadcast(broadcastIntent)
                }


            }
        }
        serialHelper.setDataBits(8)
        serialHelper.setStopBits(1)
        serialHelper.setParity(0)
        try {
            serialHelper.open()
        } catch (e: IOException) {
            FileLogger.log(applicationContext, "onStartCommand ttyS1", e.message.toString())
            e.printStackTrace()
            stopSelf(startId)
        }
        serialHelperConveyorDoor = object : SerialHelper("dev/ttyS0", 9600) {
            public override fun onDataReceived(comBean: ComBean) {
                val dataReceive = ByteUtil.ByteArrToHex(comBean.bRec)
                FileLogger.log(applicationContext, "onDataReceived ttyS0",dataReceive.toString() )
                val broadcastIntent = Intent("com.washcloud.conveyor_door_status")
                if (dataReceive == "FEFA8E070105000000010175B6") {
                    FileLogger.log(applicationContext, "SerialPortService", "Conveyor door opened")
                    broadcastIntent.putExtra("status", "open")
                    sendBroadcast(broadcastIntent)
                    openConveyorDoor()
                } else if (dataReceive == "FEFA8E030205012F0B") {
                    FileLogger.log(applicationContext, "SerialPortService", "Conveyor door closed")
                    broadcastIntent.putExtra("status", "close")
                    sendBroadcast(broadcastIntent)
                    closeConveyorDoor()
                }
            }
        }
        serialHelperConveyorDoor.setDataBits(8)
        serialHelperConveyorDoor.setStopBits(1)
        serialHelperConveyorDoor.setParity(0)
        try {
            serialHelperConveyorDoor.open()
        } catch (e: IOException) {
            FileLogger.log(applicationContext, "onStartCommand ttyS0 error", e.message.toString())
            e.printStackTrace()
            stopSelf(startId)
        }

        serialHelperConveyor = object : SerialHelper("dev/ttyS3", 19200) {
            public override fun onDataReceived(comBean: ComBean) {
                val dataReceive = ByteUtil.ByteArrToHex(comBean.bRec)
                val broadcastIntent = Intent("com.washcloud.conveyor_move")
                if (dataReceive == "0110620100020FB0") {
                    serialHelper.sendHex("01066002001037C6")
                } else if (dataReceive == "01066002001037C6") {
                    broadcastIntent.putExtra("status", "open")
                    sendBroadcast(broadcastIntent)
                } else if (dataReceive == "01066002002037D2") {
                    position = 0
                    broadcastIntent.putExtra("status", "close")
                    sendBroadcast(broadcastIntent)
                }
            }
        }
        serialHelperConveyor.setDataBits(8)
        serialHelperConveyor.setStopBits(1)
        serialHelperConveyor.setParity(0)
        try {
            serialHelperConveyor.open()
        } catch (e: IOException) {
            FileLogger.log(applicationContext, "onStartCommand ttyS3 error", e.message.toString())
            e.printStackTrace()
            stopSelf(startId)
        } finally {
            moveConveyorToZero()
        }

        return START_STICKY
    }

    private fun openConveyorDoor() {
        if (!serialHelperConveyorDoor.isOpen) {
            try {
                serialHelperConveyorDoor.open()
            } catch (e: IOException) {
                FileLogger.log(applicationContext, "openConveyorDoor error", e.message.toString())
            } finally {
                FileLogger.log(applicationContext, "openConveyorDoor","connection open and send open" )
                serialHelperConveyorDoor.sendHex("FEFA040A01000105F4012003000068A2")

            }
        } else {
            FileLogger.log(applicationContext, "openConveyorDoor","connection open before" )
            serialHelperConveyorDoor.sendHex("FEFA040A01000105F4012003000068A2")
        }
    }

    private fun closeConveyorDoor() {
        if (!serialHelperConveyorDoor.isOpen) {
            try {
                serialHelperConveyorDoor.open()
            } catch (e: IOException) {
            } finally {
                FileLogger.log(applicationContext, "closeConveyorDoor","connection open and send open" )
                serialHelperConveyorDoor.sendHex("FEFA040602000105E803E639")
            }
        } else {
            FileLogger.log(applicationContext, "closeConveyorDoor","connection open before" )
            serialHelperConveyorDoor.sendHex("FEFA040A01000105F4012003000068A2")
        }
    }

    private fun openConveyor(conveyorNumber: Int) {

        FileLogger.log(applicationContext, "SerialPortService", "Trying to open conveyor door $conveyorNumber")
        if (!serialHelperConveyor.isOpen) {
            try {
                serialHelperConveyor.open()
                FileLogger.log(applicationContext, "SerialPortService", "Opening conveyor door $conveyorNumber")
            } catch (e: IOException) {
                FileLogger.log(applicationContext, "SerialPortService", "Error opening conveyor door $conveyorNumber")
            }
            moveConveyor(conveyorNumber)
        } else {
            moveConveyor(conveyorNumber)
        }
    }

    private fun moveConveyor(targetPoint: Int) {
        FileLogger.log(applicationContext, "SerialPortService", "Moving conveyor to point $targetPoint")
        val prefix = "01106201000204" + calculatePulseNumber(targetPoint * 3 - 1)
        FileLogger.log(applicationContext, "moveConveyor", "Moving conveyor to prefix $prefix")
        val data: ByteArray = hexStringToByteArray(prefix)
        val crc: Int = compute(data)
        FileLogger.log(applicationContext, "moveConveyor", "Moving conveyor to hex ${prefix + toHex(crc)}")
        serialHelperConveyor.sendHex(prefix + toHex(crc))
        FileLogger.log(applicationContext, "moveConveyor", "Moving conveyor to location 01066002001037C6")
        serialHelperConveyor.sendHex("01066002001037C6")
    }

    private fun calculatePulseNumber(targetPoint: Int): String {
        FileLogger.log(applicationContext, "SerialPortService", "Calculating pulse number for point $targetPoint")
        val pulsePerPoint = 20000

//        val pulseNumber = if (determineMovementDirection(targetPoint)) {
//            FileLogger.log(applicationContext, "moveConveyor", "Moving conveyor Forward direction")
//            // Forward direction
//            pulsePerPoint * targetPoint
//        } else {
//            // Reverse direction with compensation offset
//            FileLogger.log(applicationContext, "moveConveyor", "Moving conveyor Reverse direction with compensation offset")
//            pulsePerPoint * targetPoint - 3000
//        }
        val pulseNumber =  pulsePerPoint * targetPoint
        FileLogger.log(applicationContext, "SerialPortService", "Pulse number for point $targetPoint is $pulseNumber")

        return String.format("%08X", pulseNumber)
    }

    private fun moveConveyorToZero() {

        FileLogger.log(applicationContext, "SerialPortService", "Moving conveyor to point 0")
        if (!serialHelperConveyor.isOpen) {
            try {
                serialHelperConveyor.open()
            } catch (e: IOException) {
            }
            serialHelperConveyor.sendHex("01066203025866E8")
            serialHelperConveyor.sendHex("01066002002037D2")
        } else {
            serialHelperConveyor.sendHex("01066002002037D2")
        }
    }

    private fun determineMovementDirection(targetPoint: Int): Boolean {
        FileLogger.log(applicationContext, "SerialPortService", "Determining movement direction to point $targetPoint")
        val forwardSteps = (targetPoint - position + holeNumber) % holeNumber
        val reverseSteps = (position - targetPoint + holeNumber) % holeNumber

        FileLogger.log(applicationContext, "SerialPortService", "Forward steps: $forwardSteps, Reverse steps: $reverseSteps determineMovementDirection  ${forwardSteps <= reverseSteps}")
        return forwardSteps <= reverseSteps
    }

    private fun openBox(boxId: String, stationId: String) {
        if (serialHelper.isOpen == false) {
            try {
                serialHelper.open()
            } catch (e: IOException) {
            }

            serialHelper.sendHex("900605" + stationId + boxId + "03")
            FileLogger.log(applicationContext, "SerialPortService", "Opening box serialHelper.isOpen $boxId at station $stationId")
        } else {
            serialHelper.sendHex("900605" + stationId + boxId + "03")
            FileLogger.log(applicationContext, "SerialPortService !serialHelper.isOpen", "Opening box $boxId at station $stationId")
        }
    }

    private fun checkBox(boxId: String, stationId: String) {
        if (!serialHelper.isOpen) {
            try {
                serialHelper.open()
            } catch (e: IOException) {

            }
            serialHelper.sendHex("900612" + stationId + boxId + "03")
            FileLogger.log(applicationContext, "SerialPortService", "Checking box serialHelper.isOpen $boxId at station $stationId")
        } else {
            serialHelper.sendHex("900612" + stationId + boxId + "03")
            FileLogger.log(applicationContext, "SerialPortService", "Checking box $boxId at station $stationId")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serialHelper.close()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tp.xmaihh.serialport.SerialHelper
import tp.xmaihh.serialport.bean.ComBean
import tp.xmaihh.serialport.utils.ByteUtil
import java.io.IOException
import java.util.Locale


class SerialPortService : Service() {
    private lateinit var serialHelper: SerialHelper
    private lateinit var serialHelperConveyor: SerialHelper
    private  lateinit var serialHelperConveyorDoor: SerialHelper
    private  lateinit var serialHelperScanner: SerialHelper
    private  var position: Int = 0
    private  val holeNumber: Int = 201


    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            FileLogger.log(applicationContext, "SerialPortService", "Received intent: ${intent.action}")
            when (intent.action) {
                "com.washcloud.open_door" -> handleOpenBox(intent, context)
                "com.washcloud.check_door" -> handleCheckBox(intent,context)
                "com.washcloud.conveyor_open" -> openConveyor(intent.getStringExtra("conveyorNumber")?.toInt())
                "com.washcloud.conveyor_close" -> closeConveyorDoor()
                "com.washcloud.conveyor_open_door" -> openConveyorDoor()
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        registerReceiver(dataReceiver, IntentFilter().apply {
            addAction("com.washcloud.open_door")
            addAction("com.washcloud.check_door")
            addAction("com.washcloud.conveyor_open")
            addAction("com.washcloud.conveyor_close")
            addAction("com.washcloud.conveyor_open_door")
        })
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        initializeSerialPorts(startId)
        return START_STICKY
    }

    private fun initializeSerialPorts(startId: Int) {
        serialHelper = createSerialHelper("dev/ttyS1", 9600, startId) { handleSerialData(it) }
        serialHelperConveyorDoor = createSerialHelper("dev/ttyS0", 9600, startId) { handleConveyorDoorData(it) }
        serialHelperScanner = createSerialHelper("dev/ttyS4", 115200, startId) { handleScanner(it) }
        serialHelperConveyor = createSerialHelperForConveyor("dev/ttyS3", 19200, startId) { handleConveyorData(it) }

    }

    private fun createSerialHelper(port: String, baudRate: Int, startId: Int, onDataReceived: (ComBean) -> Unit) = object : SerialHelper(port, baudRate) {
        override fun onDataReceived(comBean: ComBean) = onDataReceived(comBean)
    }.apply {
        dataBits = 8
        stopBits = 1
        parity = 0
        safeOpen(port, startId)
    }

    private fun createSerialHelperForConveyor(port: String, baudRate: Int, startId: Int, onDataReceived: (ComBean) -> Unit): SerialHelper {
        return object : SerialHelper(port, baudRate) {
            override fun onDataReceived(comBean: ComBean) = onDataReceived(comBean)

            override fun open() {
                super.open()
                FileLogger.log(
                    applicationContext,
                    "SerialPortService",
                    "Port $port opened, sending initial command"
                )
                sendHex("0106600F0708A43F")
                GlobalScope.launch {
                    delay(100)
                    sendHex("01066002002037D2")
                }
            }
        }.apply {
            dataBits = 8
            stopBits = 1
            parity = 0
            try {
                open()
            } catch (e: IOException) {
                FileLogger.log(applicationContext, "SerialPortService", "Error opening $port: ${e.message}")
                stopSelf(startId)
            }
        }
    }

    private fun SerialHelper.safeOpen(port: String, startId: Int) {
        try {
            open()
            FileLogger.log(applicationContext, "SerialPortService", "$port opened successfully")
        } catch (e: IOException) {
            FileLogger.log(applicationContext, "SerialPortService", "Error opening $port: ${e.message}")
            stopSelf(startId)
        }
    }

    private fun sendConveyorDoorCommand(command: String) {
        if (!serialHelperConveyorDoor.isOpen) serialHelperConveyorDoor.safeOpen("dev/ttyS0", 0)
        serialHelperConveyorDoor.sendHex(command)
    }

    private fun openConveyorDoor() {
        FileLogger.log(applicationContext, "openConveyorDoor", "Sending open command")
        sendConveyorDoorCommand("FEFA040A01000105F4012003000068A2")
    }

    private fun closeConveyorDoor() {
        FileLogger.log(applicationContext, "closeConveyorDoor", "Sending close command")
        sendConveyorDoorCommand("FEFA040602000105E803E639")
    }

    private fun openConveyor(conveyorNumber: Int?) {
        conveyorNumber?.let {
            FileLogger.log(applicationContext, "SerialPortService", "Opening conveyor door $it")
            if (!serialHelperConveyor.isOpen) serialHelperConveyor.safeOpen("dev/ttyS3", 0)
            moveConveyor(it)
        }
    }

    private fun moveConveyor(targetPoint: Int) {
        val prefix = "01106201000204" + calculatePulseNumber(targetPoint * 3 - 1)
        val data: ByteArray = hexStringToByteArray(prefix)
        val crc: Int = compute(data)
        val command = prefix + toHex(crc)



        serialHelperConveyor.sendHex("0106620307086584")
        GlobalScope.launch {
            delay(150)
            FileLogger.log(
                applicationContext,
                "moveConveyor",
                "Moving conveyor to $targetPoint -> $command"
            )
            serialHelperConveyor.sendHex(command)
        }
        GlobalScope.launch {
            delay(500)
            FileLogger.log(applicationContext, "moveConveyor", "Moving conveyor 01066002001037C6")
            serialHelperConveyor.sendHex("01066002001037C6")
        }


        GlobalScope.launch {
            delay(750)
            FileLogger.log(
                applicationContext,
                "checkConveyor",
                "Checking conveyor to $targetPoint -> 0103600200013BCA"
            )
            serialHelperConveyor.sendHex("0103600200013BCA")
        }
    }
    private fun calculatePulseNumber(targetPoint: Int): String =
        String.format(Locale.US,"%08X", 20000 * targetPoint)

    private fun determineMovementDirection(targetPoint: Int): Boolean {
        FileLogger.log(applicationContext, "SerialPortService", "Determining movement direction to point $targetPoint")
        val forwardSteps = (targetPoint - position + holeNumber) % holeNumber
        val reverseSteps = (position - targetPoint + holeNumber) % holeNumber

        FileLogger.log(applicationContext, "SerialPortService", "Forward steps: $forwardSteps, Reverse steps: $reverseSteps determineMovementDirection  ${forwardSteps <= reverseSteps}")
        return forwardSteps <= reverseSteps
    }




    private fun handleOpenBox(intent: Intent, context: Context) {
        val stationId = intent.getStringExtra("stationId") ?: return
        val boxId = intent.getStringExtra("boxId") ?: return
        FileLogger.log(context, "SerialPortService", "Opening box ${decimalToTwoDigitHex(boxId.toInt())} at station ${decimalToTwoDigitHex(stationId.toInt())}")
        openBox(boxId, stationId)
    }

    private fun handleCheckBox(intent: Intent, context: Context) {
        val stationId = intent.getStringExtra("stationId") ?: return
        val boxId = intent.getStringExtra("boxId") ?: return
        FileLogger.log(context, "SerialPortService", "Checking box ${decimalToTwoDigitHex(boxId.toInt())} at station ${decimalToTwoDigitHex(stationId.toInt())}")
        checkBox(boxId, stationId)
    }

    private fun handleSerialData(comBean: ComBean) {
        val data = ByteUtil.ByteArrToHex(comBean.bRec)
        if( data.startsWith("9007")) {
            val broadcastIntent = Intent("com.washcloud.door_status").apply {
                when {
                    data.startsWith("900785") -> handleDoorStatus(data, this, false)
                    data.startsWith("900792") -> handleDoorStatus(data, this, true)
                }
            }
            sendBroadcast(broadcastIntent)
        }
        FileLogger.log(applicationContext, "SerialPortService", "sendBroadcasta: $data");
    }

    private fun handleDoorStatus(data: String, intent: Intent,ischeck: Boolean = false) {
        FileLogger.log(applicationContext, "SerialPortService", "Received data: $data")
        intent.putExtra("stationId", hexToDecimal(data.substring(6, 8)).toString())
        if(!ischeck){
            intent.putExtra("boxId", hexToDecimal(data.substring(8, 10)).toString())
            intent.putExtra("status", data.substring(10, 12) == "01")
        }else{
            intent.putExtra("boxId", hexToDecimal(data.substring(10, 12)).toString())
            intent.putExtra("status", data.substring(8, 10) == "01")
        }

        intent.putExtra("data", data)
    }

    private fun openBox(boxId: String, stationId: String) {
        FileLogger.log(applicationContext, "openBox", "open box send data: 900605$stationId${boxId}03")
        sendCommand(serialHelper, "900605${decimalToTwoDigitHex(stationId.toInt())}${decimalToTwoDigitHex(boxId.toInt())}03")
    }

    private fun checkBox(boxId: String, stationId: String) {
        FileLogger.log(applicationContext, "checkBox", "check box send data: 900612$stationId${boxId}03")
        sendCommand(serialHelper, "900612${decimalToTwoDigitHex(stationId.toInt())}${decimalToTwoDigitHex(boxId.toInt())}03")
    }

    private fun moveConveyorToZero() {
        sendCommand(serialHelperConveyor, "01066203070866E8")
        sendCommand(serialHelperConveyor, "01066002002037D2")
    }

    private fun sendCommand(helper: SerialHelper, command: String) {
        FileLogger.log(applicationContext, "SerialPortService", "Sending command: $command")
        if (!helper.isOpen) {
            try {
                FileLogger.log(applicationContext, "SerialPortService", "Opening port")
                helper.open()
            } catch (e: IOException) {
                FileLogger.log(applicationContext, "SerialPortService", "Error opening port: ${e.message}")
                return
            }
        }
        helper.sendHex(command)
    }

    private fun handleScanner(comBean: ComBean) {
        FileLogger.log(applicationContext, "SerialPortService", "Scanner data: ${String(comBean.bRec, Charsets.UTF_8)}")
        val broadcastIntent = Intent("com.washcloud.scanner_data")
        broadcastIntent.putExtra("scannerData", String(comBean.bRec, Charsets.UTF_8))
        sendBroadcast(broadcastIntent)
        FileLogger.log(applicationContext, "SerialPortService", "sendBroadcast: ${String(comBean.bRec, Charsets.UTF_8)}");
    }


    private fun handleConveyorDoorData(comBean: ComBean) {
        val dataReceive = ByteUtil.ByteArrToHex(comBean.bRec)
        FileLogger.log(applicationContext, "SerialPortService", "Conveyor door data: $dataReceive")
        if(dataReceive == "FEFA8E070105000000010175B6" || dataReceive == "FEFA8E030205012F0B") {
            val broadcastIntent = Intent("com.washcloud.conveyor_door_status")
            when (dataReceive) {
                "FEFA8E070105000000010175B6" -> {
                    FileLogger.log(applicationContext, "SerialPortService", "Conveyor door status: open")
                    broadcastIntent.putExtra("status", "open")

                }

                "FEFA8E030205012F0B" -> {
                    FileLogger.log(applicationContext, "SerialPortService", "Conveyor door status: close")
                    broadcastIntent.putExtra("status", "close")
                }
            }
            sendBroadcast(broadcastIntent)
        }
    }

    private fun handleConveyorData(comBean: ComBean) {
        val dataReceive = ByteUtil.ByteArrToHex(comBean.bRec)
        FileLogger.log(applicationContext, "SerialPortService", "Conveyor data: $dataReceive")
        if(dataReceive.startsWith("010302")){
            if(dataReceive.substring(6, 10) != "0000"){
                GlobalScope.launch {
                    delay(1000)
                    serialHelperConveyor.sendHex("0103600200013BCA")
                }
            }else{
                val broadcastIntent = Intent("com.washcloud.conveyor_move")
                broadcastIntent.putExtra("status", "open")
                sendBroadcast(broadcastIntent)
            }
        }
        if(dataReceive.startsWith("01066002001037")) {
            val broadcastIntent = Intent("com.washcloud.conveyor_move")
            when (dataReceive) {
                "01066002002037D2" -> broadcastIntent.putExtra("status", "close")
                    .also { position = 0 }
            }
            sendBroadcast(broadcastIntent)
        }
    }

    private fun decimalToTwoDigitHex(value: Int): String {
        return String.format("%02X", value)
    }

    private fun hexToDecimal(hex: String): Int {
        return hex.toInt(16)
    }

    override fun onDestroy() {
        super.onDestroy()
        serialHelper.close()
        serialHelperConveyor.close()
        serialHelperConveyorDoor.close()
        unregisterReceiver(dataReceiver)
    }

    override fun onBind(intent: Intent): IBinder? = null
}
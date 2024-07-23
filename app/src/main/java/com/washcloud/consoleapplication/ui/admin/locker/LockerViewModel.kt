package com.washcloud.consoleapplication.ui.admin.locker

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.washcloud.consoleapplication.di.DatabaseModule
import com.washcloud.consoleapplication.hardware.SerialPortService
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tp.xmaihh.serialport.SerialHelper
import tp.xmaihh.serialport.bean.ComBean
import tp.xmaihh.serialport.utils.ByteUtil
import android_serialport_api.SerialPortFinder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class LockerViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val context: Context = getApplication<Application>().applicationContext

    private val boxDao = DatabaseModule.provideConsoleDatabase(context).getBoxDao()

    private val _lockers = MutableStateFlow<List<BoxDto>>(emptyList())
    val lockers: StateFlow<List<BoxDto>> = _lockers.asStateFlow()

    private val serialPortFinder: SerialPortFinder = SerialPortFinder()
    private val serialHelper: SerialHelper

    val ports: Array<String>
    val botes: Array<String>
    val databits: Array<String>
    val paritys: Array<String>
    val stopbits: Array<String>
    val flowcons: Array<String>

    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.washcloud.door_status") {
                val stationId = intent.getStringExtra("stationId")
                val boxId = intent.getStringExtra("boxId")
                val isOpen = intent.getBooleanExtra("status", false)
                val dataReceive = intent.getStringExtra("data")
                handleBroadcastData(stationId, boxId, isOpen, dataReceive)
            }
        }
    }

    init {
        fetchLockers()
        serialHelper = object : SerialHelper("dev/ttyS1", 115200) {
            override fun onDataReceived(comBean: ComBean) {
                handleSerialData(comBean)
            }
        }
        registerReceiver()

        ports = serialPortFinder.getAllDevicesPath()
        botes = arrayOf("0", "50", "75", "110", "134", "150", "200", "300", "600", "1200", "1800", "2400", "4800", "9600", "19200", "38400", "57600", "115200", "230400", "460800", "500000", "576000", "921600", "1000000", "1152000", "1500000", "2000000", "2500000", "3000000", "3500000", "4000000", "CUSTOM")
        databits = arrayOf("8", "7", "6", "5")
        paritys = arrayOf("NONE", "ODD", "EVEN", "SPACE", "MARK")
        stopbits = arrayOf("1", "2")
        flowcons = arrayOf("NONE", "RTS/CTS", "XON/XOFF")
    }

    private fun fetchLockers() {
        viewModelScope.launch {
            _lockers.value = boxDao.getAllBoxes()
        }
    }

    private fun handleSerialData(comBean: ComBean) {
        val receivedData = if (!toHex()) {
            String(comBean.bRec, StandardCharsets.UTF_8)
        } else {
            ByteUtil.ByteArrToHex(comBean.bRec)
        }

        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, "Data received: $receivedData", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleBroadcastData(stationId: String?, boxId: String?, isOpen: Boolean, dataReceive: String?) {
        val status = if (isOpen) "Open" else "Close"
        val message = "Tx4:<==$dataReceive $stationId $boxId $status"
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun sendCommand(action: String, stationId: String, boxId: String) {
        val intent = Intent(action).apply {
            putExtra("stationId", stationId)
            putExtra("boxId", boxId)
        }
        context.sendBroadcast(intent)
    }

    fun openSerialPort(port: String, baudRate: Int) {
        serialHelper.close()
        serialHelper.setPort(port)
        serialHelper.setBaudRate(baudRate)
        try {
            serialHelper.open()
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Serial port opened successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Failed to open serial port: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun closeSerialPort() {
        serialHelper.close()
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, "Serial port closed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerReceiver() {
        val filter = IntentFilter("com.washcloud.door_status")
        context.registerReceiver(dataReceiver, filter)
    }

    private fun unregisterReceiver() {
        context.unregisterReceiver(dataReceiver)
    }

    private fun toHex(): Boolean {

        return true
    }

    override fun onCleared() {
        super.onCleared()
        serialHelper.close()
        unregisterReceiver()
    }
}

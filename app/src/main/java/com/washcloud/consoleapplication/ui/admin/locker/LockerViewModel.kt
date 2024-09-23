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
import com.washcloud.consoleapplication.local.database.utils.BoxState
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class LockerViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val context: Context = getApplication<Application>().applicationContext

    private val boxDao = DatabaseModule.provideConsoleDatabase(context).getBoxDao()

    private val _lockers = MutableStateFlow<List<BoxDto>>(emptyList())
    val lockers: StateFlow<List<BoxDto>> = _lockers.asStateFlow()



    init {
        fetchLockers()

    }

    private fun fetchLockers() {
        viewModelScope.launch {
            _lockers.value = boxDao.getAllBoxes()
        }
    }

    fun sendCommand(action: String, stationId: String, boxId: String) {
        val intent = Intent(action).apply {
            putExtra("stationId", stationId)
            putExtra("boxId", boxId)
        }
        context.sendBroadcast(intent)
    }

    fun openAllEmptyLockers() {
        viewModelScope.launch {
            lockers.value.filter { it.boxState == BoxState.AVAILABLE }.forEach { locker ->
                sendCommand("com.washcloud.open_door", locker.stationId.toString(), locker.boxId.toString())
            }
        }
    }

    fun openAllOccupiedLockers() {
        viewModelScope.launch {
            lockers.value.filter { it.boxState == BoxState.OCCUPIED }.forEach { locker ->
                sendCommand("com.washcloud.open_door", locker.stationId.toString(), locker.boxId.toString())
            }
        }
    }




}

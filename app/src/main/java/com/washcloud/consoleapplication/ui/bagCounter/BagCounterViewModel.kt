package com.washcloud.consoleapplication.ui.bagCounter

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.webkit.URLUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.washcloud.consoleapplication.local.preferences.PrefsManager
import com.washcloud.consoleapplication.repository.BroadcastReceiverRepository
import com.washcloud.consoleapplication.utils.FileLogger
import com.washcloud.consoleapplication.utils.isGuid
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BagCounterViewModel @Inject constructor(
    private val broadcastReceiverRepository: BroadcastReceiverRepository,
    application: Application
) : AndroidViewModel(application) {

    private val context: Context = getApplication<Application>().applicationContext

    private val _sessionCount = MutableStateFlow(0)
    val sessionCount: StateFlow<Int> get() = _sessionCount

    private val _totalCount = MutableStateFlow(loadTotalCountFromPrefs())
    val totalCount: StateFlow<Int> get() = _totalCount

    fun registerScannerDataReceiver() {
        val filter = IntentFilter().apply {
            addAction("com.washcloud.scanner_data")
        }
        broadcastReceiverRepository.registerReceiver(filter)

        viewModelScope.launch {
            broadcastReceiverRepository.broadcastFlow.collectLatest { intent ->
                if (intent.action == "com.washcloud.scanner_data") {
                    val scannerData = intent.getStringExtra("scannerData")?.trim()

                    scannerData?.let { code ->
                        if (code.isGuid()) {
                            FileLogger.log(context, "BagCounterViewModel", "Bag scanned: $code")
                            onBagScanned()
                        } else {
                            FileLogger.log(context, "BagCounterViewModel", "Ignored non-GUID scan: $code")
                        }
                    }

                }
            }
        }
    }

    fun unregisterScannerDataReceiver() {
        broadcastReceiverRepository.unregisterReceiver()
        FileLogger.log(context, "BagCounterViewModel", "Unregistered scanner data receiver")
    }

    fun onBagScanned() {
        _sessionCount.value += 1
        val newTotal = _totalCount.value + 1
        _totalCount.value = newTotal
        saveTotalCountToPrefs(newTotal)

    }

    private fun loadTotalCountFromPrefs(): Int {
        // replace with your PrefsManager or SharedPreferences logic
        return PrefsManager.getTotalBagsCount(context)
    }

    private fun saveTotalCountToPrefs(value: Int) {
        PrefsManager.setTotalBagsCount(context, value)
    }

    fun resetSession() {
        _sessionCount.value = 0
        saveTotalCountToPrefs(0)
    }
}
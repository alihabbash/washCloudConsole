package com.washcloud.consoleapplication.ui.startStaff

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DateTimeViewModel @Inject constructor() : ViewModel() {

    private val _hoursText = MutableStateFlow("")
    val hoursText = _hoursText.asStateFlow()
    private val _fullDateText = MutableStateFlow("")
    val fullDateText = _fullDateText.asStateFlow()

    init {
        startTimeWatcher()
    }

    private fun startTimeWatcher() {
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            while (true) {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH)+1
                val year = calendar.get(Calendar.YEAR)
                _hoursText.value ="%02d:%02d".format(hour, minute)
                _fullDateText.value = "%02d/%02d/%02d".format(day, month, year)
                delay(2000)
            }
        }
    }
}
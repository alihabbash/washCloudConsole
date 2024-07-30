package com.washcloud.consoleapplication.ui.common

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class TimerViewModel @Inject constructor() : ViewModel() {

    private val _timerText = MutableStateFlow("")
    val timerText = _timerText.asStateFlow()
    private var timer: CountDownTimer? = null
    init {
        startTimeWatcher()
    }

    fun startTimeWatcher() {
        cancelTimer() // Cancel any existing timer
        timer = object : CountDownTimer(1000*60*4, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                val minutesRemaining = secondsRemaining / 60
                val seconds = secondsRemaining % 60
                // Update a TextView or perform other actions based on remaining seconds
              //  println("$secondsRemaining seconds remaining")
                _timerText.value ="%02d:%02d".format(minutesRemaining, seconds)
            }

            override fun onFinish() {
                _timerText.value = "0"
                timer = null // Clear reference to avoid leaks
            }
        }
        timer?.start()
    }

    fun cancelTimer() {
        timer?.cancel()
        timer = null
    }

    fun resetTimer(){
        _timerText.value = ""
    }
}
package com.washcloud.consoleapplication.ui.common

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.local.preferences.DELAY_MILLIS_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
@HiltViewModel
class TimerViewModel @Inject constructor(

    application: Application
) : AndroidViewModel(application) {

    private val _timerText = MutableStateFlow("")
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    val timerText = _timerText.asStateFlow()
    private var timer: CountDownTimer? = null
    private var remainingTime: Long = 0
    private var timerPaused = false
    private var resumeJob: Job? = null
    init {
        startTimeWatcher()
    }

    fun startTimeWatcher() {
        cancelTimer() //
        remainingTime = getStoredDelayTime()
        timerPaused = false
        createTimer(remainingTime)

    }

    private fun getStoredDelayTime(): Long {
        val delayString = sharedPreferences.getString(DELAY_MILLIS_KEY, "600000") ?: "600000"
        return delayString.toLongOrNull() ?: 600000L
    }

    private fun createTimer(timeInMillis: Long) {
        timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                val secondsRemaining = millisUntilFinished / 1000
                val minutesRemaining = secondsRemaining / 60
                val seconds = secondsRemaining % 60
                _timerText.value = String.format(Locale.US, "%02d:%02d", minutesRemaining, seconds)
            }

            override fun onFinish() {
                _timerText.value = "0"
                timer = null
            }
        }
        timer?.start()
    }

    fun pauseTimer() {
        timer?.cancel()
        timerPaused = true
    }

    fun resumeTimerAfterDelay(delayMillis: Long) {
        resumeJob?.cancel()
        resumeJob = CoroutineScope(Dispatchers.Main).launch {
            delay(6000L)
            if (timerPaused) {
                createTimer(remainingTime)
                timerPaused = false
            }
        }
    }

    fun cancelTimer() {
        timer?.cancel()
        timer = null
    }

    fun resetTimer(){
        _timerText.value = ""
    }
}
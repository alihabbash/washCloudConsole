package com.washcloud.consoleapplication.ui.common

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.local.preferences.DELAY_MILLIS_KEY
import com.washcloud.consoleapplication.local.preferences.TIMER_SECONDS_KEY
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
        cancelTimer()
        resumeJob?.cancel()
        
        val seconds = sharedPreferences.getString(TIMER_SECONDS_KEY, "180")?.toLongOrNull() ?: 180L
        remainingTime = seconds * 1000L
        timerPaused = false

        _timerText.value = "" // Clear text during inactivity delay
        
        // Start 5 second inactivity delay
        resumeJob = CoroutineScope(Dispatchers.Main).launch {
            delay(5000L)
            createTimer(remainingTime)
        }
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
        resumeJob?.cancel() // Also cancel the inactivity watcher
        timerPaused = true
        _timerText.value = "" // Clear the timer text
    }

    fun resumeTimerAfterDelay(delayMillis: Long) {
        // We completely ignore the passed input `delayMillis` now because
        // we want a consistent 5 second delay and full 3 minute restart.
        startTimeWatcher()
    }

    fun cancelTimer() {
        timer?.cancel()
        timer = null
    }

    fun resetTimer(){
        _timerText.value = ""
    }
}
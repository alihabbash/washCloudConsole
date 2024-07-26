package com.washcloud.consoleapplication

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.ui.mainad.MainAdActivity
import com.washcloud.consoleapplication.local.preferences.language
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject

import androidx.work.*
import com.washcloud.consoleapplication.local.preferences.API_KEY
import com.washcloud.consoleapplication.local.preferences.TERMINAL_SN
import com.washcloud.consoleapplication.ui.mainad.SendHeartbeatWorker
import java.util.concurrent.TimeUnit


@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }


    private fun scheduleHeartbeatWorker() {
        val workRequest = PeriodicWorkRequestBuilder<SendHeartbeatWorker>(3, TimeUnit.MINUTES)
            .setInputData(
                workDataOf(
                    "API_KEY" to API_KEY,
                    "TERMINAL_SN" to TERMINAL_SN
                )
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SendHeartbeatWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        var lang = sharedPreferences.getString(language, "")
        if(lang?.isNotEmpty() != true) {
            lang = Locale.getDefault().language
        }

        MainActivity.dLocale = Locale(lang)
        MainAdActivity.dLocale = Locale(lang)
    }
}
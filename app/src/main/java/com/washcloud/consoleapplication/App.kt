package com.washcloud.consoleapplication

import android.app.Application
import android.view.View
import androidx.hilt.work.HiltWorkerFactory
import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.ui.mainad.MainAdActivity
import com.washcloud.consoleapplication.local.preferences.language
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject

import androidx.work.*


import java.util.concurrent.TimeUnit


import android.content.Context
import org.acra.ACRA
import org.acra.ReportField
import org.acra.config.CoreConfigurationBuilder
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.ReportSenderFactory

@HiltAndroidApp
class App : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            reportContent = listOf(
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE,
                ReportField.LOGCAT,
            )

        }
    }


    override fun onCreate() {
        super.onCreate()

        setupCrashRebootHandler()

        MainActivity.dLocale = Locale("ar")
        MainAdActivity.dLocale = Locale("ar")
        
        scheduleDailyCustomerSync()
    }

    private fun scheduleDailyCustomerSync() {
        val calendar = java.util.Calendar.getInstance()
        val currentMillis = calendar.timeInMillis

        // Target next 12:00 or 00:00
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        if (hour < 12) {
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 12)
        } else {
            // Next midnight
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        }
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)

        val initialDelay = calendar.timeInMillis - currentMillis

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 12 hours apart covers exactly 12:00 and 00:00
        val syncWorkRequest = PeriodicWorkRequestBuilder<com.washcloud.consoleapplication.workmanager.CustomerSyncWorker>(
            12, TimeUnit.HOURS
        )
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .setConstraints(constraints)
        .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyCustomerSync",
            ExistingPeriodicWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }

    /**
     * Sets up a global uncaught exception handler that reboots the device
     * via root access when a fatal crash occurs.
     *
     * Includes a crash-loop guard: if two crashes occur within 60 seconds,
     * the reboot is skipped to prevent an infinite reboot loop.
     */
    private fun setupCrashRebootHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            val prefs = getSharedPreferences("crash_reboot_prefs", Context.MODE_PRIVATE)
            val lastCrashTime = prefs.getLong("last_crash_time", 0L)
            val now = System.currentTimeMillis()

            val shouldReboot = now - lastCrashTime > 60_000L

            if (shouldReboot) {
                prefs.edit().putLong("last_crash_time", now).commit()
                android.util.Log.e("App", "Fatal crash detected, rebooting device...", exception)

                // Fire reboot immediately. exec() spawns a separate OS process
                // that survives even after our app process is killed.
                // ACRA still gets ~100-200ms to save the crash report to disk.
                try {
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot"))
                } catch (e: Exception) {
                    android.util.Log.e("App", "Failed to execute reboot command", e)
                }
            } else {
                android.util.Log.e("App", "Crash-loop detected, skipping reboot.", exception)
            }

            // Let ACRA / default handler process the crash (saves report to disk)
            defaultHandler?.uncaughtException(thread, exception)
        }
    }
}
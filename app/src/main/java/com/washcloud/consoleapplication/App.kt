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
class App : Application() {
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


        MainActivity.dLocale = Locale("ar")
        MainAdActivity.dLocale = Locale("ar")

    }
}
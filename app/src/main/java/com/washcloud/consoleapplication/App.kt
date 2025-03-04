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


@HiltAndroidApp
class App : Application() {


    override fun onCreate() {
        super.onCreate()


        MainActivity.dLocale = Locale("ar")
        MainAdActivity.dLocale = Locale("ar")

    }
}
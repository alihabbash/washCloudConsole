package com.washcloud.consoleapplication

import android.app.Application
import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.mainad.MainAdActivity
import com.washcloud.consoleapplication.utils.language
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class App : Application() {

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
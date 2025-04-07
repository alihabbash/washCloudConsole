package com.washcloud.consoleapplication.hardware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.washcloud.consoleapplication.MainActivity
import com.washcloud.consoleapplication.ui.mainad.MainAdActivity
import com.washcloud.consoleapplication.utils.FileLogger

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        FileLogger.log(context,"BootReceiver", "intent.action  ${intent.action} receive")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            FileLogger.log(context,"BootReceiver", "onReceive: Boot completed");


            Handler(Looper.getMainLooper()).postDelayed({
                FileLogger.log(context,"BootReceiver", "onReceive: Starting MainActivity");
                val i = Intent(context, MainAdActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(i)
            }, 100)

        }
    }
}

package com.washcloud.consoleapplication

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class KioskDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }
}

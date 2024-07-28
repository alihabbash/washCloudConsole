package com.washcloud.consoleapplication.hardware

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.washcloud.consoleapplication.ui.mainad.MainAdActivity

class UsbBroadcastReceiver(
    private val usbManager: UsbManager,
    private val mainActivity: MainAdActivity
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        when (action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                device?.let {
                    if (usbManager.hasPermission(it)) {
                        mainActivity.handleDeviceConnection(it)
                    } else {
                        val permissionIntent = PendingIntent.getBroadcast(
                            context,
                            0,
                            Intent(ACTION_USB_PERMISSION),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        usbManager.requestPermission(it, permissionIntent)
                    }
                }
            }

            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                device?.let {
                    mainActivity.handleDeviceDisconnection(it)
                }
            }
        }
    }

    companion object {
        const val ACTION_USB_PERMISSION = "com.washcloud.consoleapplication.USB_PERMISSION"
    }

}
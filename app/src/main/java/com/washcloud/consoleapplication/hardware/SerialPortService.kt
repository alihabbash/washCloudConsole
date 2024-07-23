package com.washcloud.consoleapplication.hardware

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import tp.xmaihh.serialport.SerialHelper
import tp.xmaihh.serialport.bean.ComBean
import tp.xmaihh.serialport.utils.ByteUtil
import java.io.IOException



class SerialPortService : Service() {
    private lateinit var serialHelper: SerialHelper
    private val dataReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.washcloud.open_door") {
                val stationId = intent.getStringExtra("stationId")
                val boxId = intent.getStringExtra("boxId")
                openBox(boxId, stationId)
            } else if (intent.action == "com.washcloud.check_door") {
                val stationId = intent.getStringExtra("stationId")
                val boxId = intent.getStringExtra("boxId")
                checkBox(boxId, stationId)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter()
        filter.addAction("com.washcloud.open_door")
        filter.addAction("com.washcloud.check_door")

       registerReceiver(dataReceiver, filter)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        serialHelper = object : SerialHelper("dev/ttyS1", 9600) {
            public override fun onDataReceived(comBean: ComBean) {
                val dataReceive = ByteUtil.ByteArrToHex(comBean.bRec)
                val broadcastIntent = Intent("com.washcloud.door_status")
                if (dataReceive.startsWith("900785")) {
                    broadcastIntent.putExtra("stationId", dataReceive.substring(6, 8))
                    broadcastIntent.putExtra("boxId", dataReceive.substring(8, 10))
                    val isOpen = dataReceive.substring(10, 12) == "01"
                    broadcastIntent.putExtra("status", isOpen)
                    broadcastIntent.putExtra("data", dataReceive)
                    sendBroadcast(broadcastIntent)
                } else if (dataReceive.startsWith("900792")) {
                    broadcastIntent.putExtra("stationId", dataReceive.substring(6, 8))
                    broadcastIntent.putExtra("boxId", dataReceive.substring(10, 12))
                    val isOpen = dataReceive.substring(8, 10) == "01"
                    broadcastIntent.putExtra("status", isOpen)
                    broadcastIntent.putExtra("data", dataReceive)
                    sendBroadcast(broadcastIntent)
                }
            }
        }
        serialHelper.setDataBits(8)
        serialHelper.setStopBits(1)
        serialHelper.setParity(0)
        try {
          serialHelper.open()
        } catch (e: IOException) {
            e.printStackTrace()
            stopSelf(startId)
        }

        return START_STICKY
    }

    private fun openBox(boxId: String?, stationId: String?) {
        if (serialHelper.isOpen == false) {
            try {
                serialHelper.open()
            } catch (e: IOException) {
            }
            serialHelper.sendHex("900605" + stationId + boxId + "03")
        } else {
            serialHelper.sendHex("900605" + stationId + boxId + "03")
        }
    }

    private fun checkBox(boxId: String?, stationId: String?) {
        if (serialHelper.isOpen == false) {
            try {
                serialHelper.open()
            } catch (e: IOException) {
            }
            serialHelper.sendHex("900612" + stationId + boxId + "03")
        } else {
            serialHelper.sendHex("900612" + stationId + boxId + "03")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serialHelper.close()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
package com.washcloud.consoleapplication.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class BroadcastReceiverRepository(private val context: Context) {

    private val _broadcastFlow = MutableSharedFlow<Intent>(extraBufferCapacity = 64)
    val broadcastFlow: SharedFlow<Intent> = _broadcastFlow

    private val myBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.e("BroadcastReceiverRepository","BroadcastReceiverRepository.onReceive: $intent")
            intent?.let {
                // Handle the received intent and emit it to the flow
                val emitted = _broadcastFlow.tryEmit(it)
                Log.e("BroadcastReceiverRepository", "tryEmit result: $emitted");
            }
        }
    }

    fun registerReceiver(intentFilter: IntentFilter) {
        context.applicationContext.registerReceiver(myBroadcastReceiver, intentFilter)
    }

    fun unregisterReceiver() {
        try {
            context.applicationContext.unregisterReceiver(myBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered
        }
    }
}
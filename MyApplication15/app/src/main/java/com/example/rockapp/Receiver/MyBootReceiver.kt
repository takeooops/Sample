package com.example.rockapp.Receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.rockapp.MyAccessibilityService

class MyBootReceiver : BroadcastReceiver()  {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            ContextCompat.startForegroundService(context, Intent(context, MyAccessibilityService::class.java))
        }
    }
}

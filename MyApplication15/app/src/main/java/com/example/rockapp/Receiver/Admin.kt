package com.example.rockapp.Receiver

import android.app.admin.DeviceAdminReceiver;
import android.content.Context
import android.content.Intent

class Admin : DeviceAdminReceiver(){
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        // 有効化されたとき
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        // 無効化されたとき
    }
}
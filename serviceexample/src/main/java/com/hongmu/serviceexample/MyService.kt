package com.hongmu.serviceexample

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast

class MyService : Service() {

    companion object {
        const val TAG = ".MyService"
        const val ACTION_START = "com.hongmu.serviceexample.START"
        const val ACTION_RUN = "com.hongmu.serviceexample.RUN"
        const val ACTION_STOP = "com.hongmu.serviceexample.STOP"
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate: 서비스 생성")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "onStartCommand: $action")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: 서비스 종료")
        super.onDestroy()
    }
}
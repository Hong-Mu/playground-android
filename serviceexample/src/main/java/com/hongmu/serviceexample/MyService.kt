package com.hongmu.serviceexample

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class MyService : Service() {

    companion object {
        const val TAG = ".MyService"
        const val ACTION_START = "com.hongmu.serviceexample.START"
        const val ACTION_RUN = "com.hongmu.serviceexample.RUN"
        const val ACTION_STOP = "com.hongmu.serviceexample.STOP"
    }

    inner class MyBinder : Binder() {
        fun getService() = this@MyService
    }

    private val binder = MyBinder()

    fun getMessage(): String {
        return "Hello from Service"
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind")
        return binder
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "onStartCommand: $action")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)
    }
}
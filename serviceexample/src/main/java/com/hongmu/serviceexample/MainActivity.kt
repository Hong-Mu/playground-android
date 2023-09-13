package com.hongmu.serviceexample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.hongmu.serviceexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = ".MainActivity"
    }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var myService: MyService
    private var isBind = false
    val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
            val binder = iBinder as MyService.MyBinder
            myService = binder.getService()
            isBind = true
            Log.d(TAG, "onServiceConnected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBind = false
            Log.d(TAG, "onServiceDisconnected")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener { startService() }
        binding.btnStop.setOnClickListener { stopService() }
        binding.btnBind.setOnClickListener { bindService() }
        binding.btnUnbind.setOnClickListener { unbindService() }
        binding.btnCall.setOnClickListener { callServiceFunction() }
        binding.btnStartForeground.setOnClickListener {
            val intent = Intent(this, MyForegroundService::class.java)
            ContextCompat.startForegroundService(this, intent)
        }
        binding.btnStopForeground.setOnClickListener {
            val intent = Intent(this, MyForegroundService::class.java)
            stopService(intent)
        }
    }

    private fun startService() {
        startService(Intent(this, MyService::class.java).apply {
            action = MyService.ACTION_START
        })
    }

    private fun stopService() {
        stopService(Intent(this, MyService::class.java))
    }

    private fun bindService() {
        val intent = Intent(this, MyService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        if (isBind) {
            unbindService(connection)
            isBind = false
        }
    }

    private fun callServiceFunction() {
        if(isBind) {
            val message = myService.getMessage()
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "서비스가 연결되지 않음", Toast.LENGTH_SHORT).show()
        }
    }
}
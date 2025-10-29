package com.example.lab_week_08.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.lab_week_08.MainActivity
import com.example.lab_week_08.R

class SecondNotificationService : Service() {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var serviceHandler: Handler

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationBuilder = startForegroundService()
        // Gunakan nama thread yang berbeda
        val handlerThread = HandlerThread("ThirdThread")
            .apply { start() }
        serviceHandler = Handler(handlerThread.looper)
    }

    private fun startForegroundService(): NotificationCompat.Builder {
        val pendingIntent = getPendingIntent()
        val channelId = createNotificationChannel()
        val notificationBuilder = getNotificationBuilder(
            pendingIntent, channelId
        )
        // Gunakan ID Notifikasi yang unik
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        return notificationBuilder
    }

    private fun getPendingIntent(): PendingIntent {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getActivity(
            this, 0, Intent(
                this,
                MainActivity::class.java
            ), flag
        )
    }

    private fun createNotificationChannel(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel ID baru
            val channelId = "002"
            val channelName = "002 Channel"
            val channelPriority = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                channelId,
                channelName,
                channelPriority
            )
            val service = requireNotNull(
                ContextCompat.getSystemService(
                    this,
                    NotificationManager::class.java
                )
            )
            service.createNotificationChannel(channel)
            channelId
        } else {
            ""
        }

    private fun getNotificationBuilder(
        pendingIntent: PendingIntent, channelId: String
    ) =
        NotificationCompat.Builder(this, channelId)
            // Teks notifikasi baru
            .setContentTitle("Third worker process is done")
            .setContentText("All processes are complete!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setTicker("All processes complete!")
            .setOngoing(true)


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val returnValue = super.onStartCommand(
            intent,
            flags, startId
        )
        // Gunakan EXTRA_ID yang unik
        val Id = intent?.getStringExtra(EXTRA_ID)
            ?: throw IllegalStateException("Channel ID must be provided")

        serviceHandler.post {
            // Panggil fungsi countdown yang baru
            countDownFromFiveToZero(notificationBuilder)
            notifyCompletion(Id)

            // stopForeground(STOP_FOREGROUND_REMOVE) // Tetap dikomentari
            stopSelf()
        }
        return returnValue
    }

    // Fungsi countdown baru (timer 5 detik)
    private fun countDownFromFiveToZero(
        notificationBuilder:
        NotificationCompat.Builder
    ) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as
                NotificationManager
        // Timer 5 detik
        for (i in 5 downTo 0) {
            Thread.sleep(1000L)
            // Teks countdown baru
            notificationBuilder.setContentText("$i seconds until final report")
                .setSilent(true)
            notificationManager.notify(
                NOTIFICATION_ID,
                notificationBuilder.build()
            )
        }
    }

    private fun notifyCompletion(Id: String) {
        Handler(Looper.getMainLooper()).post {
            // Panggil LiveData yang unik
            mutableID.value = Id
        }
    }

    // Companion object yang unik
    companion object {
        const val NOTIFICATION_ID = 0xCA8 // ID Baru
        const val EXTRA_ID = "Id_02" // ID Baru

        private val mutableID = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableID
    }
}
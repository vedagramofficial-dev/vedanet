package com.vedanet.app.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.vedanet.app.MainActivity
import com.vedanet.app.data.ProfileStore
import com.vedanet.app.xray.XrayManager

class VedaNetVpnService : VpnService() {

    private var tunInterface: ParcelFileDescriptor? = null
    private lateinit var xrayManager: XrayManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        xrayManager = XrayManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> startVpn(intent.getStringExtra(EXTRA_PROFILE_ID))
            ACTION_DISCONNECT -> stopVpn()
            else -> stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun startVpn(profileId: String?) {
        val store = ProfileStore(this)
        val profile = store.load().firstOrNull { it.id == profileId }
            ?: store.load().firstOrNull()

        if (profile == null) {
            stopVpn()
            return
        }

        val configureIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        startForeground(
            NOTIFICATION_ID,
            buildNotification("VedaNet is connecting"),
            foregroundServiceType()
        )

        runCatching {
            xrayManager.start(profile)

            val builder = Builder()
                .setSession("VedaNet")
                .setMtu(1500)
                .addAddress("10.111.0.2", 32)
                .addDnsServer("1.1.1.1")
                .addRoute("0.0.0.0", 0)
                .setConfigureIntent(configureIntent)

            tunInterface = builder.establish()

            if (tunInterface == null) {
                stopVpn()
                return
            }

            isRunning = true
            updateNotification("VedaNet is connected")
        }.onFailure {
            stopVpn()
        }
    }

    private fun stopVpn() {
        runCatching {
            xrayManager.stop()
        }

        runCatching {
            tunInterface?.close()
        }

        tunInterface = null
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VedaNet")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "VedaNet VPN",
            NotificationManager.IMPORTANCE_LOW
        )

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun foregroundServiceType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
    }

    companion object {
        const val ACTION_CONNECT = "com.vedanet.app.action.CONNECT"
        const val ACTION_DISCONNECT = "com.vedanet.app.action.DISCONNECT"
        const val EXTRA_PROFILE_ID = "extra_profile_id"

        private const val CHANNEL_ID = "vedanet_vpn"
        private const val NOTIFICATION_ID = 1001

        @Volatile
        var isRunning: Boolean = false
            private set
    }
}
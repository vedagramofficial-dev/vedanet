package com.vedanet.app.vpn

import android.content.Intent
import android.net.VpnService
import android.os.IBinder

class VedaNetVpnService : VpnService() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }
}
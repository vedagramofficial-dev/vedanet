package com.vedanet.app.vpn

import android.content.Context
import android.content.Intent

object VpnController {

    fun connect(context: Context, profileId: String?) {
        val intent = Intent(context, VedaNetVpnService::class.java).apply {
            action = VedaNetVpnService.ACTION_CONNECT
            putExtra(VedaNetVpnService.EXTRA_PROFILE_ID, profileId)
        }
        context.startService(intent)
    }

    fun disconnect(context: Context) {
        val intent = Intent(context, VedaNetVpnService::class.java).apply {
            action = VedaNetVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }
}
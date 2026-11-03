package com.vedanet.app.xray

import android.content.Context
import com.vedanet.app.data.Profile
import com.vedanet.app.data.Protocol
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class XrayPlan(
    val profile: Profile,
    val localSocksPort: Int,
    val configFile: File
)

class XrayManager(private val context: Context) {

    private var process: Process? = null

    fun start(profile: Profile): XrayPlan {
        stop()

        val config = buildConfig(profile)
        val configFile = File(context.filesDir, "vedanet-${profile.id}.json")
        configFile.writeText(config.toString(2))

        val binary = File(context.applicationInfo.nativeLibraryDir, "libxray.so")
        val builder = ProcessBuilder(
            binary.absolutePath,
            "run",
            "-c",
            configFile.absolutePath
        )
        builder.redirectErrorStream(true)

        process = builder.start()

        return XrayPlan(
            profile = profile,
            localSocksPort = 10808,
            configFile = configFile
        )
    }

    fun stop() {
        runCatching {
            process?.destroy()
        }
        process = null
    }

    private fun buildConfig(profile: Profile): JSONObject {
        val inbound = JSONObject()
            .put("port", 10808)
            .put("listen", "127.0.0.1")
            .put("protocol", "socks")
            .put("settings", JSONObject().put("udp", true))

        val outbound = when (profile.protocol) {
            Protocol.VLESS -> buildVlessOutbound(profile)
            Protocol.VMESS -> buildVmessOutbound(profile)
            Protocol.SHADOWSOCKS -> buildShadowsocksOutbound(profile)
            Protocol.TROJAN -> buildTrojanOutbound(profile)
        }

        return JSONObject()
            .put("inbounds", JSONArray().put(inbound))
            .put("outbounds", JSONArray().put(outbound))
    }

    private fun buildVlessOutbound(profile: Profile): JSONObject {
        val user = JSONObject()
            .put("id", profile.uuidOrPassword)
            .put("encryption", "none")

        val vnext = JSONObject()
            .put("address", profile.address)
            .put("port", profile.port)
            .put("users", JSONArray().put(user))

        val stream = JSONObject().put("network", "tcp")
        if (profile.sni.isNotBlank()) {
            stream.put("security", "tls")
            stream.put(
                "tlsSettings",
                JSONObject().put("serverName", profile.sni)
            )
        }

        return JSONObject()
            .put("protocol", "vless")
            .put("settings", JSONObject().put("vnext", JSONArray().put(vnext)))
            .put("streamSettings", stream)
    }

    private fun buildVmessOutbound(profile: Profile): JSONObject {
        val user = JSONObject()
            .put("id", profile.uuidOrPassword)
            .put("alterId", 0)
            .put("security", "auto")

        val vnext = JSONObject()
            .put("address", profile.address)
            .put("port", profile.port)
            .put("users", JSONArray().put(user))

        return JSONObject()
            .put("protocol", "vmess")
            .put("settings", JSONObject().put("vnext", JSONArray().put(vnext)))
    }

    private fun buildShadowsocksOutbound(profile: Profile): JSONObject {
        val server = JSONObject()
            .put("address", profile.address)
            .put("port", profile.port)
            .put("method", "aes-256-gcm")
            .put("password", profile.uuidOrPassword)

        return JSONObject()
            .put("protocol", "shadowsocks")
            .put("settings", JSONObject().put("servers", JSONArray().put(server)))
    }

    private fun buildTrojanOutbound(profile: Profile): JSONObject {
        val server = JSONObject()
            .put("address", profile.address)
            .put("port", profile.port)
            .put("password", profile.uuidOrPassword)

        return JSONObject()
            .put("protocol", "trojan")
            .put("settings", JSONObject().put("servers", JSONArray().put(server)))
    }
}
package com.vedanet.app.data

import org.json.JSONArray
import org.json.JSONObject

enum class Protocol {
    VLESS,
    VMESS,
    SHADOWSOCKS,
    TROJAN
}

data class Profile(
    val id: String,
    val name: String,
    val protocol: Protocol,
    val address: String,
    val port: Int,
    val uuidOrPassword: String,
    val sni: String = "",
    val enabled: Boolean = true
)

object ProfileJson {
    fun encode(profiles: List<Profile>): String {
        val array = JSONArray()
        profiles.forEach { profile ->
            array.put(
                JSONObject()
                    .put("id", profile.id)
                    .put("name", profile.name)
                    .put("protocol", profile.protocol.name)
                    .put("address", profile.address)
                    .put("port", profile.port)
                    .put("secret", profile.uuidOrPassword)
                    .put("sni", profile.sni)
                    .put("enabled", profile.enabled)
            )
        }
        return array.toString()
    }

    fun decode(raw: String): List<Profile> {
        if (raw.isBlank()) return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                Profile(
                    id = item.optString("id"),
                    name = item.optString("name"),
                    protocol = runCatching {
                        Protocol.valueOf(item.optString("protocol"))
                    }.getOrDefault(Protocol.VLESS),
                    address = item.optString("address"),
                    port = item.optInt("port", 443),
                    uuidOrPassword = item.optString("secret"),
                    sni = item.optString("sni"),
                    enabled = item.optBoolean("enabled", true)
                )
            }
        }.getOrDefault(emptyList())
    }
}
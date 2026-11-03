package com.vedanet.app.data

import android.content.Context
import java.util.UUID

class ProfileStore(context: Context) {

    private val preferences = context.getSharedPreferences(
        "vedanet_profiles",
        Context.MODE_PRIVATE
    )

    fun load(): List<Profile> {
        val raw = preferences.getString(KEY_PROFILES, "") ?: ""
        return ProfileJson.decode(raw)
    }

    fun save(profiles: List<Profile>) {
        preferences.edit()
            .putString(KEY_PROFILES, ProfileJson.encode(profiles))
            .apply()
    }

    fun add(
        name: String,
        protocol: Protocol,
        address: String,
        port: Int,
        secret: String,
        sni: String
    ): Profile {
        val profile = Profile(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Profile" },
            protocol = protocol,
            address = address.trim(),
            port = port,
            uuidOrPassword = secret,
            sni = sni.trim()
        )

        val updated = load() + profile
        save(updated)
        return profile
    }

    fun delete(id: String) {
        save(load().filterNot { it.id == id })
    }

    companion object {
        private const val KEY_PROFILES = "profiles"
    }
}
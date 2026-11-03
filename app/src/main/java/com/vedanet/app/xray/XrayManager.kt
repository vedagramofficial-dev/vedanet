package com.vedanet.app.xray

import com.vedanet.app.data.Profile

class XrayManager {

    private var activeProfile: Profile? = null

    fun prepare(profile: Profile): XrayPlan {
        require(profile.address.isNotBlank()) { "Server address is required" }
        require(profile.port in 1..65535) { "Invalid server port" }

        activeProfile = profile
        return XrayPlan(
            profile = profile,
            localSocksPort = 10808,
            configName = "vedanet-${profile.id}.json"
        )
    }

    fun stop() {
        activeProfile = null
    }
}

data class XrayPlan(
    val profile: Profile,
    val localSocksPort: Int,
    val configName: String
)
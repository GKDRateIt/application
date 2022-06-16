package com.github.gdkrateit.service

import io.javalin.http.Context
import kotlinx.serialization.Serializable

@Serializable
data class HeartbeatData(val status: String) {
    companion object {
        val HEALTHY = HeartbeatData("healthy")
    }
}

class HeartbeatHandler : ApiBase() {
    override val method: HttpMethod
        get() = HttpMethod.GET
    override val path: String
        get() = "/heartbeat"

    override fun handle(ctx: Context) {
        ctx.kotlinxJson(HeartbeatData.HEALTHY)
    }
}

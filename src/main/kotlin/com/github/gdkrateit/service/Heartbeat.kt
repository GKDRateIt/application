package com.github.gdkrateit.service

import io.javalin.http.Context
import kotlinx.serialization.Serializable

@Serializable
data class HeartbeatResponse(
    override val status: ResponseStatus,
    override val detail: String
) : ApiResponseBase() {
    companion object {
        val HEALTHY = HeartbeatResponse(ResponseStatus.SUCCESS, "Nothing special")
    }
}

class HeartbeatHandler : ApiBase() {
    override val method: HttpMethod
        get() = HttpMethod.GET
    override val path: String
        get() = "/heartbeat"

    override fun handle(ctx: Context) {
        ctx.kotlinxJson(HeartbeatResponse.HEALTHY)
    }
}

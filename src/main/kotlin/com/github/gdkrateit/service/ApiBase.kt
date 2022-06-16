package com.github.gdkrateit.service

import io.javalin.http.ContentType
import io.javalin.http.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class HttpMethod {
    GET, POST
}

abstract class ApiBase {
    abstract val method: HttpMethod
    abstract val path: String
    abstract fun handle(ctx: Context)

    /**
     * Reply [obj] in JSON format.
     * This is a special extension function which uses kotlinx
     * type-safe serialization.
     * */
    inline fun <reified T : Any> Context.kotlinxJson(obj: T) {
        this.contentType(ContentType.JSON)
        this.result(Json.encodeToString(obj))
    }
}
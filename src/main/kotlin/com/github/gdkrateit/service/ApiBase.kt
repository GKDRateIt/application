package com.github.gdkrateit.service

import io.javalin.http.ContentType
import io.javalin.http.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class HttpMethod {
    GET, POST
}

@Serializable
enum class ResponseStatus {
    SUCCESS, FAIL
}

/**
 * All rest apis are finished with HTTP POST.
 * Post form looks like:
 * ```Json
 * {
 *     action: "...",
 *     // other fields
 * }
 * ```
 * */
@Serializable
enum class CrudVerb {
    CREATE, READ, UPDATE, DELETE
}

@Serializable
abstract class ApiResponseBase {
    abstract val status: ResponseStatus
    abstract val detail: String

    companion object {
        val NotImplemented = object : ApiResponseBase() {
            override val status: ResponseStatus
                get() = ResponseStatus.FAIL
            override val detail: String
                get() = "This API is not implemented yet"

        }

        fun illegalParam(
            illegalParamNames: Collection<String>,
            extraInfo: String = ""
        ): ApiResponseBase {
            return object : ApiResponseBase() {
                override val status: ResponseStatus
                    get() = ResponseStatus.FAIL
                override val detail: String
                    get() = run {
                        val sb = StringBuilder()
                        sb.append("Illegal parameter names: ")
                        illegalParamNames.forEach {
                            sb.append("$it ")
                        }
                        sb.append('.')
                        sb.append(extraInfo)
                        sb.toString()
                    }
            }
        }
    }
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

    fun Context.notImplemented() {
        this.kotlinxJson(ApiResponseBase.NotImplemented)
    }

    fun Context.illegalParam(
        illegalParamNames: Collection<String>,
        extraInfo: String = ""
    ) {
        this.kotlinxJson(ApiResponseBase.illegalParam(illegalParamNames, extraInfo))
    }

    fun Context.illegalParam(
        illegalParamName: String,
        extraInfo: String = ""
    ) {
        this.kotlinxJson(ApiResponseBase.illegalParam(listOf(illegalParamName), extraInfo))
    }
}
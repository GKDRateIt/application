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
class ApiResponse<T>(
    val status: ResponseStatus,
    val detail: String,
    val data: T?,
) {
    companion object {
        val NotImplementedError = ApiResponse<Any>(ResponseStatus.FAIL, "Not Implemented yet.", null)

        fun illegalParamError(
            illegalParamNames: Collection<String>,
            extraInfo: String = ""
        ): ApiResponse<Any> {
            val sb = StringBuilder()
            sb.append("Illegal parameter names: ")
            illegalParamNames.forEach {
                sb.append("$it ")
            }
            sb.append('.')
            sb.append(extraInfo)
            val detail = sb.toString()
            return ApiResponse(ResponseStatus.FAIL, detail, null)
        }

        inline fun <reified T> success(replyData: T?, detail: String = ""): ApiResponse<T> {
            return ApiResponse(ResponseStatus.SUCCESS, detail, replyData)
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

    fun Context.notImplementedError() {
        this.kotlinxJson(ApiResponse.NotImplementedError)
    }

    fun Context.illegalParamError(
        illegalParamNames: Collection<String>,
        extraInfo: String = ""
    ) {
        this.kotlinxJson(ApiResponse.illegalParamError(illegalParamNames, extraInfo))
    }

    fun Context.illegalParamError(
        illegalParamName: String,
        extraInfo: String = ""
    ) {
        this.kotlinxJson(ApiResponse.illegalParamError(listOf(illegalParamName), extraInfo))
    }

    inline fun <reified T> Context.successReply(data: T, detail: String = "") {
        this.kotlinxJson(ApiResponse.success(replyData = data, detail = detail))
    }
}

abstract class CrudApiBase : ApiBase() {
    override val method: HttpMethod
        get() = HttpMethod.POST

    abstract fun handleCreate(ctx: Context)
    abstract fun handleRead(ctx: Context)
    abstract fun handleUpdate(ctx: Context)
    abstract fun handleDelete(ctx: Context)

    override fun handle(ctx: Context) {
        val actionRaw = ctx.formParam("action")
        if (actionRaw == null) {
            ctx.illegalParamError("action", "missing this parameter.")
            return
        }
        when (actionRaw.uppercase()) {
            "CREATE" -> handleCreate(ctx)
            "READ" -> handleRead(ctx)
            "UPDATE" -> handleUpdate(ctx)
            "DELETE" -> handleDelete(ctx)
            else -> ctx.illegalParamError(
                "action",
                "Argument action must be one of create/read/update/delete"
            )
        }
    }
}
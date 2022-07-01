package com.github.gkdrateit.service

import io.javalin.http.ContentType
import io.javalin.http.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.*

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
)

abstract class ApiBase {
    abstract val method: HttpMethod
    abstract val path: String
    abstract fun handle(ctx: Context)

    protected val logger = LoggerFactory.getLogger("")

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
        this.kotlinxJson(ApiResponse<Any>(ResponseStatus.FAIL, "Not Implemented yet.", null))
    }

    fun Context.illegalParamError(
        illegalParamNames: List<String>,
        extraInfo: String = ""
    ) {
        val sb = StringBuilder()
        sb.append("Illegal parameter names: ")
        illegalParamNames.forEach {
            sb.append("$it ")
        }
        sb.append('.')
        sb.append(extraInfo)
        val detail = sb.toString()
        val response = ApiResponse<String>(ResponseStatus.FAIL, detail, null)
        this.kotlinxJson(response)
    }

    fun Context.illegalParamError(
        illegalParamName: String,
        extraInfo: String = ""
    ) {
        this.illegalParamError(listOf(illegalParamName), extraInfo)
    }

    fun Context.missingParamError(name: String) {
        this.illegalParamError(name, "Must provide parameter `$name`.")
    }

    fun Context.base64Error(name: String) {
        this.illegalParamError(name, "Parameter `$name` must be a valid base64 string.")
    }

    fun Context.base64Error(name: List<String>) {
        val sb = StringBuilder()
        sb.append("Parameters in `")
        name.forEach { sb.append("$it, ") }
        sb.append("` must be valid base64 strings.")
        this.illegalParamError(name, extraInfo = sb.toString())
    }

    fun Context.success(detail: String = "") {
        this.kotlinxJson(ApiResponse<String>(ResponseStatus.SUCCESS, detail, null))
    }

    inline fun <reified T> Context.successReply(data: T, detail: String = "") {
        this.kotlinxJson(ApiResponse(ResponseStatus.SUCCESS, detail, data))
    }

    fun Context.databaseError(detail: String = "") {
        this.kotlinxJson(ApiResponse<String>(ResponseStatus.FAIL, detail, null))
    }
}

abstract class CrudApiBase : ApiBase() {
    override val method: HttpMethod
        get() = HttpMethod.POST

    protected val base64Decoder: Base64.Decoder = Base64.getDecoder()
    protected val base64Encoder: Base64.Encoder = Base64.getEncoder()

    abstract fun handleCreate(ctx: Context)
    abstract fun handleRead(ctx: Context)
    abstract fun handleUpdate(ctx: Context)
    abstract fun handleDelete(ctx: Context)

    override fun handle(ctx: Context) {
        logger.info("Received request from ${ctx.req.remoteAddr}:${ctx.req.remotePort}")
        val actionRaw = ctx.formParam("_action")
        if (actionRaw == null) {
            ctx.missingParamError("_action")
            return
        }
        when (actionRaw.uppercase()) {
            "CREATE" -> handleCreate(ctx)
            "READ" -> handleRead(ctx)
            "UPDATE" -> handleUpdate(ctx)
            "DELETE" -> handleDelete(ctx)
            else -> ctx.illegalParamError(
                "_action",
                "Argument action must be one of create/read/update/delete"
            )
        }
    }
}
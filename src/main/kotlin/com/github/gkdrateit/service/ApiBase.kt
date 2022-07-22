package com.github.gkdrateit.service

import io.javalin.http.Context
import org.slf4j.LoggerFactory
import java.util.*

enum class HttpMethod {
    GET, POST
}

//@Serializable
enum class ResponseStatus {
    SUCCESS, FAIL
}

//@Serializable
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

    fun notImplementedError(): ApiResponse<String> {
        return ApiResponse(ResponseStatus.FAIL, "Not Implemented yet.", null)
    }

    fun illegalParamError(
        illegalParamNames: List<String>,
        extraInfo: String = ""
    ): ApiResponse<String> {
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

    fun illegalParamError(
        illegalParamName: String,
        extraInfo: String = ""
    ): ApiResponse<String> {
        return illegalParamError(listOf(illegalParamName), extraInfo)
    }

    fun missingParamError(name: String): ApiResponse<String> {
        return illegalParamError(name, "Must provide parameter `$name`.")
    }

    fun base64Error(name: String): ApiResponse<String> {
        return illegalParamError(name, "Parameter `$name` must be a valid base64 string.")
    }

    fun base64Error(name: List<String>): ApiResponse<String> {
        val sb = StringBuilder()
        sb.append("Parameters in `")
        name.forEach { sb.append("$it, ") }
        sb.append("` must be valid base64 strings.")
        return illegalParamError(name, extraInfo = sb.toString())
    }

    fun success(detail: String = ""): ApiResponse<String> {
        return ApiResponse(ResponseStatus.SUCCESS, detail, null)
    }

    inline fun <reified T> successReply(data: T, detail: String = ""): ApiResponse<T> {
        return ApiResponse(ResponseStatus.SUCCESS, detail, data)
    }

    fun databaseError(detail: String = ""): ApiResponse<String> {
        return ApiResponse(ResponseStatus.FAIL, detail, null)
    }
}

abstract class CrudApiBase : ApiBase() {
    override val method: HttpMethod
        get() = HttpMethod.POST

    protected val base64Decoder: Base64.Decoder = Base64.getDecoder()
    protected val base64Encoder: Base64.Encoder = Base64.getEncoder()

    abstract fun handleCreate(param: Map<String, String>): ApiResponse<*>
    abstract fun handleRead(param: Map<String, String>): ApiResponse<*>
    abstract fun handleUpdate(param: Map<String, String>): ApiResponse<*>
    abstract fun handleDelete(param: Map<String, String>): ApiResponse<*>

    override fun handle(ctx: Context) {
        logger.info("Received request from ${ctx.req.remoteAddr}:${ctx.req.remotePort}")
        val form = ctx.bodyAsClass<HashMap<String, String>>()
        when (form["_action"]?.uppercase()) {
            null -> missingParamError("_action")
            "CREATE" -> handleCreate(form)
            "READ" -> handleRead(form)
            "UPDATE" -> handleUpdate(form)
            "DELETE" -> handleDelete(form)
            else -> illegalParamError(
                "_action",
                "Argument action must be one of create/read/update/delete"
            )
        }.let {
            ctx.json(it)
        }
    }
}
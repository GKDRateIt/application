package com.github.gkdrateit.service

import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

enum class HttpMethod {
    GET, POST
}

enum class ResponseStatus {
    SUCCESS, FAIL
}

open class ApiResponse<T>(
    val status: ResponseStatus,
    val detail: String,
    val data: T?,
    val totalCount: Long? = null,
    val offset: Long? = null,
    val limit: Int? = null,
)

class PaginationInfo(val offset: Long, val limit: Int)

class ApiPaginationResponse<T>(
    status: ResponseStatus,
    detail: String, data: T?,
    totalCount: Long,
    offset: Long,
    limit: Int,

    ) : ApiResponse<T>(
    status, detail, data, totalCount, offset, limit,
) {
    constructor(
        status: ResponseStatus,
        detail: String,
        data: T?,
        totalCount: Long,
        paginationInfo: PaginationInfo,
    ) : this(status, detail, data, totalCount, paginationInfo.offset, paginationInfo.limit)
}

abstract class ApiBase {
    abstract val method: HttpMethod
    abstract val path: String
    abstract fun handle(ctx: Context)

    protected val logger: Logger = LoggerFactory.getLogger("")

    protected val base64Decoder: Base64.Decoder = Base64.getDecoder()
    protected val base64Encoder: Base64.Encoder = Base64.getEncoder()

    companion object {
        const val DEFAULT_OFFSET: Long = 0
        const val DEFAULT_COUNT: Int = 5
    }

    fun notImplementedError(): ApiResponse<String> {
        return ApiResponse(ResponseStatus.FAIL, "Not Implemented yet.", null)
    }

    fun illegalParamError(
        illegalParamNames: List<String>, extraInfo: String = ""
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
        illegalParamName: String, extraInfo: String = ""
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

    inline fun <reified T> successReply(
        data: T,
        totalCount: Long,
        paginationInfo: PaginationInfo,
        detail: String = ""
    ): ApiPaginationResponse<T> {
        return ApiPaginationResponse(ResponseStatus.SUCCESS, detail, data, totalCount, paginationInfo)
    }

    fun userRegisteredError(): ApiResponse<String> {
        return ApiResponse(ResponseStatus.FAIL, "User Has been registered", null)
    }

    fun emailIllegalError(): ApiResponse<String> {
        return ApiResponse(ResponseStatus.FAIL, "Illegal email", null)
    }

    fun databaseError(detail: String = ""): ApiResponse<String> {
        return ApiResponse(ResponseStatus.FAIL, detail, null)
    }

    fun authError(): ApiResponse<String> {
        return ApiResponse(ResponseStatus.FAIL, "Wrong username or password", null)
    }

    fun jwtError(): ApiResponse<String> {
        return ApiResponse(ResponseStatus.FAIL, "Invalid JWT", null)
    }

    fun error(detail: String = ""): ApiResponse<String> {
        return ApiResponse(ResponseStatus.FAIL, detail, null)
    }

    fun Context.paramJsonMap(): Map<String, String> {
        return this.bodyAsClass<HashMap<String, String>>()
    }

    fun Context.javaWebToken(): String {
        return this.header("Authorization")?.substringAfter("Bearer ") ?: ""
    }

    fun getPaginationInfoOrDefault(param: Map<String, String>): PaginationInfo {
        val offset = param["offset"]?.toLong() ?: DEFAULT_OFFSET
        val limit = param["limit"]?.toInt() ?: DEFAULT_COUNT
        return PaginationInfo(offset, limit)
    }
}

abstract class CrudApiBase : ApiBase() {
    override val method: HttpMethod
        get() = HttpMethod.POST

    abstract fun handleCreate(ctx: Context): ApiResponse<*>
    abstract fun handleRead(ctx: Context): ApiResponse<*>
    abstract fun handleUpdate(ctx: Context): ApiResponse<*>
    abstract fun handleDelete(ctx: Context): ApiResponse<*>

    override fun handle(ctx: Context) {
        logger.info("Received request from ${ctx.req().remoteAddr}:${ctx.req().remotePort}")
        val param = ctx.paramJsonMap()
        when (param["_action"]?.uppercase()) {
            null -> missingParamError("_action")
            "CREATE" -> handleCreate(ctx)
            "READ" -> handleRead(ctx)
            "UPDATE" -> handleUpdate(ctx)
            "DELETE" -> handleDelete(ctx)
            else -> illegalParamError(
                "_action", "Argument action must be one of create/read/update/delete"
            )
        }.let {
            ctx.json(it)
        }
    }
}
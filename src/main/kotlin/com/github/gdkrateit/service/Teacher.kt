package com.github.gdkrateit.service

import com.github.gdkrateit.database.Teacher
import com.github.gdkrateit.database.TeacherModel
import com.github.gdkrateit.database.Teachers
import io.javalin.http.Context
import java.util.*
import kotlinx.serialization.Serializable


@Serializable
data class TeacherResponse(
    override val status: ResponseStatus,
    override val detail: String,
    val data: List<TeacherModel>
) : ApiResponseBase() {
    companion object {
        fun success(data: List<TeacherModel>): TeacherResponse {
            return TeacherResponse(
                ResponseStatus.SUCCESS,
                detail = "",
                data = data
            )
        }
    }
}

class TeacherHandler : ApiBase() {
    override val method: HttpMethod
        get() = HttpMethod.POST
    override val path: String
        get() = "teacher"

    override fun handle(ctx: Context) {
        // the `?: run {...}` block executes when the expr before `?:` is null
        val actionRaw = ctx.formParam("action") ?: run {
            ctx.illegalParam("action")
            return
        }
        when (actionRaw.uppercase(Locale.getDefault())) {
            "READ" -> {
                val name = ctx.formParam("name")
                val email = ctx.formParam("email")
                if (name == null && email == null) {
                    ctx.illegalParam(listOf("name", "email"), "One of them must be non-null.")
                }
                val selected = if (name != null) {
                    Teacher.find {
                        Teachers.name eq name
                    }
                } else {
                    Teacher.find {
                        // The `expr!!` throws exception if `expr` is null.
                        // And automatically cast it to the non-null version.
                        // But here it won't be null.
                        Teachers.email eq email!!
                    }
                }.map { it.toModel() }
                ctx.kotlinxJson(TeacherResponse.success(selected))
            }
            else -> {
                ctx.notImplemented()
            }
        }
    }
}
package com.github.gkdrateit.service

import com.github.gkdrateit.config.Config
import com.github.gkdrateit.database.DbAdapter
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.post
import org.slf4j.LoggerFactory

class ApiServer {
    val app: Javalin = Javalin.create()

    private val handlers = listOf(
        CourseHandler(),
        ReviewHandler(),
        TeacherHandler(),
        UserHandler(),
    )

    init {
        DbAdapter.connect()
        app.routes {
            ApiBuilder.path("/api") {
                handlers.forEach {
                    when (it.method) {
                        HttpMethod.GET -> get(it.path) { ctx ->
                            it.handle(ctx)
                        }
                        HttpMethod.POST -> post(it.path) { ctx ->
                            it.handle(ctx)
                        }
                    }
                }
            }
        }
    }

    fun start() {
        app.start(Config.port)
    }

    fun close() {
        app.close()
    }
}
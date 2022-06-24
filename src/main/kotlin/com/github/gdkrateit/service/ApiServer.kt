package com.github.gdkrateit.service

import com.github.gdkrateit.config.Config
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.post


class ApiServer {
    private val app = Javalin.create()

    private val handlers = listOf(
        CourseHandler(),
        ReviewHandler(),
        TeacherHandler(),
        UserHandler(),
    )

    fun start() {
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
        app.start(Config.port)
    }

    fun stop() {
        app.close()
    }
}
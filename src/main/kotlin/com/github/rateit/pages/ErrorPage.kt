package com.github.rateit.pages

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/errors")
class ErrorPage : ErrorController {
    @GetMapping("")
    fun errorPage(httpRequest: HttpServletRequest): String {
        return when (httpRequest.getAttribute("javax.servlet.error.status_code") as Int) {
            404 -> this.javaClass.getResource("/html/404.html")!!.readText()
            else -> "ERROR!!!"
        }
    }

    override fun getErrorPath(): String {
        return "/errors"
    }
}
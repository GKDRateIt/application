package com.github.rateit.pages

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*


@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "404 not found")
class NotFoundException : RuntimeException()

@Controller
@RequestMapping("/resources")
class Resources {
    @RequestMapping("/html")
    @ResponseBody
    fun loadResource(
        @RequestParam("name") name: String
    ): String {
        val logger = LoggerFactory.getLogger(this.javaClass)
        logger.info("Request $name")
        return this.javaClass.getResource("/html/${name}")?.readText()
            ?: throw NotFoundException()
    }
}
package com.github.rateit.pages

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*


@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "page not found")
class PageNotFoundException : RuntimeException()

@Controller
@RequestMapping("/common-static")
class CommonStaticPages {
    @GetMapping("/{resourceName}")
    @ResponseBody
    fun loadPage(
        @PathVariable resourceName: String
    ): String {
        return this.javaClass.getResource("/html/${resourceName}.html")?.readText()
            ?: throw PageNotFoundException()
    }
}
package com.github.rateit.pages

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller("/")
class IndexPage {
    @GetMapping("")
    @ResponseBody
    fun getIndexPage(): String {
        return this.javaClass.getResource("/html/index.html")!!.readText()
    }
}
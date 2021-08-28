package com.github.rateit

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.runApplication


@SpringBootApplication
class AppEntry

fun main(args: Array<String>) {
    runApplication<AppEntry>(*args)
}
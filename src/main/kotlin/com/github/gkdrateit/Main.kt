package com.github.gkdrateit

import com.github.gkdrateit.config.Config
import com.github.gkdrateit.service.ApiServer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

fun main(args: Array<String>) = runBlocking {
    val apiServerJob = launch { ApiServer().start() }
    val logger = LoggerFactory.getLogger("com.github.gkdrateit.main")
    apiServerJob.join()
}
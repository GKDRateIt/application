package com.github.gkdrateit

import com.github.gkdrateit.service.ApiServer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiServerJob = launch { ApiServer().start() }
    apiServerJob.join()
}
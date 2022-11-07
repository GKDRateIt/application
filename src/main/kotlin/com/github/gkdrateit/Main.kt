package com.github.gkdrateit

import com.github.gkdrateit.config.Config
import com.github.gkdrateit.service.ApiServer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {
//    if (args.isNotEmpty()) {
//        Config.configFileName = args[0]
//    }
    val apiServerJob = launch { ApiServer().start() }
    apiServerJob.join()
}
package com.github.gkdrateit.config

object Config {
    val port: Int by lazy {
        // TODO: Read config file or environment variable.
        8080
    }
}
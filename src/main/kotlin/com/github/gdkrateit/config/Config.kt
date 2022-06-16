package com.github.gdkrateit.config

object Config {
    val port: Int by lazy {
        // TODO: Read com.github.gdkrateit.config file or environment variable.
        8080
    }
}
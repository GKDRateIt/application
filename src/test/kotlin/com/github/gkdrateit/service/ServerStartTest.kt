package com.github.gkdrateit.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class ServerStartTest {
    @Test
    fun startClose() = runBlocking {
        val app = ApiServer()
        try {
            app.start()
        } catch (e: Throwable) {
            assert(false)
        } finally {
            app.close()
        }
    }
}
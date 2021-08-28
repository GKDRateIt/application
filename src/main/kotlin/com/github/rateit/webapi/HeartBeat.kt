package com.github.rateit.webapi

import com.github.rateit.heartbeat.HealthyChecker
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.atomic.AtomicLong

data class HeartBeat(val status: String, val times: Long)

@RestController
@RequestMapping("/api")
class HeartBeatApi {
    val times = AtomicLong()

    @GetMapping("/are-u-ok")
    fun getHeartBeatPage(): HeartBeat {
        return HeartBeat(HealthyChecker.systemStatus(), times.incrementAndGet())
    }
}
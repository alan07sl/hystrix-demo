package com.alan07sl.hystrix

class RemoteServiceTestSimulator internal constructor(private val wait: Long) {
    fun execute(): String {
        Thread.sleep(wait)
        return "Success"
    }
}

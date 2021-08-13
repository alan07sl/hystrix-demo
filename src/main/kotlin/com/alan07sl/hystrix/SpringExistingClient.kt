package com.alan07sl.hystrix

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component("springClient")
class SpringExistingClient(
    @Value("\${remoteservice.timeout}") private val remoteServiceDelay: Long
) {

    @HystrixCircuitBreaker
    fun invokeRemoteServiceWithHystrix(): String {
        return RemoteServiceTestSimulator(remoteServiceDelay).execute()
    }

    fun invokeRemoteServiceWithOutHystrix(): String {
        return RemoteServiceTestSimulator(remoteServiceDelay).execute()
    }
}

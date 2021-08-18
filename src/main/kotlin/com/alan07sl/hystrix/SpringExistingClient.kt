package com.alan07sl.hystrix

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component("springClient")
class SpringExistingClient(
    @Value("\${remoteservice.timeout}") private val remoteServiceDelay: Long
) {

    /**
     * Esta anotación custom permite ejecutar el servicio remoto
     * envuelto por Hystrix Circuit Breaker
     * @see HystrixAspect
     */
    @HystrixCircuitBreaker
    fun invokeRemoteServiceWithHystrix(): String {
        return RemoteServiceTestSimulator(remoteServiceDelay).execute()
    }

    fun invokeRemoteServiceWithOutHystrix(): String {
        return RemoteServiceTestSimulator(remoteServiceDelay).execute()
    }
}

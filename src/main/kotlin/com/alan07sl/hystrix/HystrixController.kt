package com.alan07sl.hystrix

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HystrixController(
    @Autowired
    private val client: SpringExistingClient
) {

    @RequestMapping("/withHystrix")
    fun withHystrix(): String {
        return client.invokeRemoteServiceWithHystrix()
    }

    @RequestMapping("/withOutHystrix")
    fun withOutHystrix(): String {
        return client.invokeRemoteServiceWithOutHystrix()
    }
}

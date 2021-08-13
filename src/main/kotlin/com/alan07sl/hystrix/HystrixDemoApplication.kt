package com.alan07sl.hystrix

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean

@SpringBootApplication
class HystrixDemoApplication

fun main(args: Array<String>) {
    runApplication<HystrixDemoApplication>(*args)
}

@Bean
fun adminServletRegistrationBean(): ServletRegistrationBean<*> {
    return ServletRegistrationBean(HystrixMetricsStreamServlet(), "/hystrix.stream")
}

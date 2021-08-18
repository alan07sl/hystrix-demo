package com.alan07sl.hystrix

import com.netflix.hystrix.exception.HystrixRuntimeException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import kotlin.system.measureTimeMillis

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [HystrixDemoApplication::class])
class SpringAndHystrixIntegrationTest {

    @Autowired
    private lateinit var hystrixController: HystrixController

    @Test
    fun givenTimeOutOf15000_whenClientCalledWithHystrix_thenExpectHystrixRuntimeException() {
        val time = measureTimeMillis {
            Assertions.assertThrows(HystrixRuntimeException::class.java) {
                hystrixController.withHystrix()
            }
        }
        println("Tiempo esperando respuesta: $time")
    }

    @Test
    fun givenTimeOutOf15000_whenClientCalledWithOutHystrix_thenExpectSuccess() {
        val time = measureTimeMillis {
            assertThat(hystrixController.withOutHystrix(), equalTo("Success"))
        }
        println("Tiempo esperando respuesta: $time")
    }
}

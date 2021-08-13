package com.alan07sl.hystrix

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolProperties
import com.netflix.hystrix.exception.HystrixRuntimeException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class HystrixTimeoutManualTest {

    @Test
    fun givenInputAlanAndDefaultSettings_whenCommandExecuted_thenReturnHelloAlan() {
        assertThat(CommandHelloWorld("Alan").execute(), equalTo("Hello Alan!"))
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenSvcTimeoutOf100AndDefaultSettings_whenRemoteSvcExecuted_thenReturnSuccess() {
        val config = HystrixCommand.Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroup2"))
        assertThat(
            RemoteServiceTestCommand(config, RemoteServiceTestSimulator(100)).execute(),
            equalTo("Success")
        )
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenSvcTimeoutOf10000AndDefaultSettings__whenRemoteSvcExecuted_thenExpectHRE() {
        val config = HystrixCommand.Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupTest3"))
        assertThrows(HystrixRuntimeException::class.java) {
            RemoteServiceTestCommand(config, RemoteServiceTestSimulator(10000)).execute()
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenSvcTimeoutOf5000AndExecTimeoutOf10000_whenRemoteSvcExecuted_thenReturnSuccess() {
        val config = HystrixCommand.Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupTest4"))
        val commandProperties = HystrixCommandProperties.Setter()
        commandProperties.withExecutionTimeoutInMilliseconds(10000)
        config.andCommandPropertiesDefaults(commandProperties)
        assertThat(
            RemoteServiceTestCommand(config, RemoteServiceTestSimulator(500)).execute(),
            equalTo("Success")
        )
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenSvcTimeoutOf15000AndExecTimeoutOf5000__whenExecuted_thenExpectHRE() {
        val config = HystrixCommand.Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupTest5"))
        val commandProperties = HystrixCommandProperties.Setter()
        commandProperties.withExecutionTimeoutInMilliseconds(5000)
        config.andCommandPropertiesDefaults(commandProperties)
        assertThrows(HystrixRuntimeException::class.java) {
            RemoteServiceTestCommand(config, RemoteServiceTestSimulator(15000)).execute()
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenSvcTimeoutOf500AndExecTimeoutOf10000AndThreadPool__whenExecuted_thenReturnSuccess() {
        val config = HystrixCommand.Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupThreadPool"))
        val commandProperties = HystrixCommandProperties.Setter()
        commandProperties.withExecutionTimeoutInMilliseconds(10000)
        config.andCommandPropertiesDefaults(commandProperties)
        config.andThreadPoolPropertiesDefaults(
            HystrixThreadPoolProperties.Setter()
                .withMaxQueueSize(10)
                .withCoreSize(3)
                .withQueueSizeRejectionThreshold(10)
        )
        assertThat(
            RemoteServiceTestCommand(config, RemoteServiceTestSimulator(500)).execute(),
            equalTo("Success")
        )
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenCircuitBreakerSetup__whenRemoteSvcCmdExecuted_thenReturnSuccess() {
        val config = HystrixCommand.Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupCircuitBreaker"))
        val properties = HystrixCommandProperties.Setter()
        properties.withExecutionTimeoutInMilliseconds(1000)
        properties.withCircuitBreakerSleepWindowInMilliseconds(4000)
        properties.withExecutionIsolationStrategy(
            HystrixCommandProperties.ExecutionIsolationStrategy.THREAD
        )
        properties.withCircuitBreakerEnabled(true)
        properties.withCircuitBreakerRequestVolumeThreshold(1)
        config.andCommandPropertiesDefaults(properties)
        config.andThreadPoolPropertiesDefaults(
            HystrixThreadPoolProperties.Setter()
                .withMaxQueueSize(1)
                .withCoreSize(1)
                .withQueueSizeRejectionThreshold(1)
        )
        assertThat(invokeRemoteService(config, 10000), equalTo(null))
        assertThat(invokeRemoteService(config, 10000), equalTo(null))
        assertThat(invokeRemoteService(config, 10000), equalTo(null))
        Thread.sleep(5000)
        assertThat(
            RemoteServiceTestCommand(config, RemoteServiceTestSimulator(500)).execute(),
            equalTo("Success")
        )
        assertThat(
            RemoteServiceTestCommand(config, RemoteServiceTestSimulator(500)).execute(),
            equalTo("Success")
        )
        assertThat(
            RemoteServiceTestCommand(
                config, RemoteServiceTestSimulator(500)
            ).execute(),
            equalTo("Success")
        )
    }

    @Throws(InterruptedException::class)
    fun invokeRemoteService(config: HystrixCommand.Setter?, timeout: Long): String? {
        var response: String? = null
        try {
            response = RemoteServiceTestCommand(
                config,
                RemoteServiceTestSimulator(timeout)
            ).execute()
        } catch (ex: HystrixRuntimeException) {
            println("ex = $ex")
        }
        return response
    }
}

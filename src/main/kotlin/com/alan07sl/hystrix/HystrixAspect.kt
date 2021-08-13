package com.alan07sl.hystrix

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolProperties
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@Aspect
class HystrixAspect(
    @Value("\${remoteservice.command.execution.timeout}") private val executionTimeout: Int,
    @Value("\${remoteservice.command.sleepwindow}") private val sleepWindow: Int,
    @Value("\${remoteservice.command.threadpool.maxsize}") private val maxThreadCount: Int,
    @Value("\${remoteservice.command.threadpool.coresize}") private val coreThreadCount: Int,
    @Value("\${remoteservice.command.task.queue.size}") private val queueCount: Int,
    @Value("\${remoteservice.command.group.key}") private val groupKey: String,
    @Value("\${remoteservice.command.key}") private val key: String,
) {
    private var config: HystrixCommand.Setter? = null
    private var commandProperties: HystrixCommandProperties.Setter? = null
    private var threadPoolProperties: HystrixThreadPoolProperties.Setter? = null

    @Around("@annotation(com.alan07sl.hystrix.HystrixCircuitBreaker)")
    fun circuitBreakerAround(aJoinPoint: ProceedingJoinPoint): Any? {
        return RemoteServiceCommand(config, aJoinPoint).execute()
    }

    @PostConstruct
    private fun setup() {
        config = HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
        config = config?.andCommandKey(HystrixCommandKey.Factory.asKey(key))
        commandProperties = HystrixCommandProperties.Setter()
        commandProperties?.withExecutionTimeoutInMilliseconds(executionTimeout)
        commandProperties?.withCircuitBreakerSleepWindowInMilliseconds(sleepWindow)
        threadPoolProperties = HystrixThreadPoolProperties.Setter()
        threadPoolProperties?.withMaxQueueSize(maxThreadCount)?.withCoreSize(coreThreadCount)?.withMaxQueueSize(queueCount)
        config?.andCommandPropertiesDefaults(commandProperties)
        config?.andThreadPoolPropertiesDefaults(threadPoolProperties)
    }

    private class RemoteServiceCommand constructor(config: Setter?, private val joinPoint: ProceedingJoinPoint) : HystrixCommand<String>(config) {
        override fun run(): String {
            return try {
                joinPoint.proceed() as String
            } catch (th: Throwable) {
                throw Exception(th)
            }
        }
    }
}

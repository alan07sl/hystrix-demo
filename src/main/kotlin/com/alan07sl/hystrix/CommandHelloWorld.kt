package com.alan07sl.hystrix

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

class CommandHelloWorld(private val name: String) : HystrixCommand<String>(HystrixCommandGroupKey.Factory.asKey("ExampleGroup")) {
    override fun run(): String {
        return "Hello $name!"
    }
}

package com.alan07sl.hystrix

import com.netflix.hystrix.HystrixCommand

internal class RemoteServiceTestCommand(config: Setter?, private val remoteService: RemoteServiceTestSimulator) : HystrixCommand<String>(config) {
    override fun run(): String {
        return remoteService.execute()
    }
}

package com.alan07sl.hystrix

import com.netflix.hystrix.HystrixCommand

/**
 * Y aquí está nuestro cliente que llama al simulador [com.alan07sl.hystrix.RemoteServiceTestSimulator]
 *
 * La llamada al servicio es aislada y envuelta en la función o método run() del HystrixCommand.
 * Es en este "envolvimiento" que provee la resiliencia
 *
 * El siguiente test demuestra como esto se consume:
 * @sample com.alan07sl.hystrix.HystrixTimeoutManualTest.givenSvcTimeoutOf100AndDefaultSettings_whenRemoteSvcExecuted_thenReturnSuccess()
 */
internal class RemoteServiceTestCommand(config: Setter?, private val remoteService: RemoteServiceTestSimulator) : HystrixCommand<String>(config) {
    override fun run(): String {
        return remoteService.execute()
    }
}

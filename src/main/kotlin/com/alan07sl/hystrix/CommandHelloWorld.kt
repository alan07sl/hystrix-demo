package com.alan07sl.hystrix

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

/**
 * La forma en que Hystrix provee tolerancia a fallos y laatencia es aislando y envolviendo
 * las llamadas a servicios remotos.
 * Este simple ejemplo envuelve una llamada en la función o método run() de HystrixCommand
 *
 * Ejecutamos el ejemplo en
 * @sample com.alan07sl.hystrix.HystrixTimeoutManualTest.givenInputAlanAndDefaultSettings_whenCommandExecuted_thenReturnHelloAlan
 */
class CommandHelloWorld(private val name: String) : HystrixCommand<String>(HystrixCommandGroupKey.Factory.asKey("ExampleGroup")) {
    override fun run(): String {
        return "Hello $name!"
    }
}

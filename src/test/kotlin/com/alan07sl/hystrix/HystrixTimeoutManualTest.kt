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
    fun givenSvcTimeoutOf100AndDefaultSettings_whenRemoteSvcExecuted_thenReturnSuccess() {
        val config = HystrixCommand.Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroup2"))
        assertThat(
            RemoteServiceTestCommand(config, RemoteServiceTestSimulator(100)).execute(),
            equalTo("Success")
        )
    }

    @Test
    fun givenSvcTimeoutOf10000AndDefaultSettings_whenRemoteSvcExecuted_thenExpectHRE() {
        val config = HystrixCommand.Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupTest3"))
        assertThrows(HystrixRuntimeException::class.java) {
            RemoteServiceTestCommand(config, RemoteServiceTestSimulator(10000)).execute()
        }
    }

    /**
     * En este test simulamos que la demora en la respuesta del servicio sea de 500 ms
     * Mientras que el timeout está configurado en 10_000 ms por lo que el servicio logra responder.
     */
    @Test
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

    /**
     * En cambio en este test simulamos que el servicio se demore 15_000 ms, mientras que
     * el tiemout está configurado en 5_000 ms por lo que podemos esperar una HystrixExecutionException.
     *
     * Este test demuestra como Hystrix no espera más allá del timeout configurado por una respuesta.
     * Lo que ayuda al sistema protegido por Hystrix a ser más "responsive".
     */
    @Test
    fun givenSvcTimeoutOf15000AndExecTimeoutOf5000_whenExecuted_thenExpectHRE() {
        val config = HystrixCommand.Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupTest5"))
        val commandProperties = HystrixCommandProperties.Setter()
        commandProperties.withExecutionTimeoutInMilliseconds(5000)
        config.andCommandPropertiesDefaults(commandProperties)
        assertThrows(HystrixRuntimeException::class.java) {
            RemoteServiceTestCommand(config, RemoteServiceTestSimulator(15000)).execute()
        }
    }

    /**
     * Cuando un servicio remoto comienza a responder lentamente, tipicamente la aplicación continua
     * llamando a dicho servicio.
     * Nuestra aplicación no conoce si el servicio remoto está degradado o no, y nuevos hilos son
     * disparados con cada petición que hacemos.
     * No queremos que esto suceda, necesitamos esos hilos para hacer llamadas a otros servicios,
     * o procesos corriendo en nuestro servidor y tampoco queremos que suba el uso de cpu por la
     * necesidad de planificar esta innecesaria cantidad de hilos.
     */
    @Test
    fun givenSvcTimeoutOf500AndExecTimeoutOf10000AndThreadPool_whenExecuted_thenReturnSuccess() {
        val config = HystrixCommand.Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupThreadPool"))
        val commandProperties = HystrixCommandProperties.Setter()
        commandProperties.withExecutionTimeoutInMilliseconds(10000)
        config.andCommandPropertiesDefaults(commandProperties)
        config.andThreadPoolPropertiesDefaults(
            HystrixThreadPoolProperties.Setter()
                // Cantidad de threads máximos en el pool
                .withMaximumSize(10)
                // Cantidad minima de threads en el pool
                .withCoreSize(3)
                // Tamaño de la cola previa al pool, este tamaño no se puede cambiar dinamicamente
                .withMaxQueueSize(10)
                // Cantidad de tareas pendientes en la cola a partir de la cual Hystrix comienza a rechazar peticiones
                .withQueueSizeRejectionThreshold(10)
        )
        assertThat(
            RemoteServiceTestCommand(config, RemoteServiceTestSimulator(500)).execute(),
            equalTo("Success")
        )
    }

    /**
     * Ahora consideremos el caso en el que el servicio remoto comienza a malfuncionar
     * En este caso queremos evitar seguir desperdiciando recursos al lanzar dichas peticiones
     * Idealmente por un cierto periodo de tiempo, de modo que el servicio en cuestión se pueda recuperar
     * Esto es lo que hace el patrón Circuit Breaker.
     */
    @Test
    fun givenCircuitBreakerSetup_whenRemoteSvcCmdExecuted_thenReturnSuccess() {
        val config = HystrixCommand.Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupCircuitBreaker"))
        val properties = HystrixCommandProperties.Setter()
        // Timeout de 1 segundo
        properties.withExecutionTimeoutInMilliseconds(1000)
        // 4 segundos de ventana de tiempo durante la cual debemos dejar de llamar al servicio
        properties.withCircuitBreakerSleepWindowInMilliseconds(4000)
        properties.withExecutionIsolationStrategy(
            HystrixCommandProperties.ExecutionIsolationStrategy.THREAD
        )
        // Habilitamos el Circuit Breaker
        properties.withCircuitBreakerEnabled(true)
        // Volumen de requests fallando para abrir el circuito
        properties.withCircuitBreakerRequestVolumeThreshold(1)
        config.andCommandPropertiesDefaults(properties)

        // Thread pool solo puede manejar un thread y una tarea pendiente en su cola
        config.andThreadPoolPropertiesDefaults(
            HystrixThreadPoolProperties.Setter()
                .withMaxQueueSize(1)
                .withCoreSize(1)
                .withQueueSizeRejectionThreshold(1)
        )

        // Disparamos tres hilos con el servicio demorandose 10 segundos, por lo cual
        // El primero irá al thread pool hasta su timeout, el segundo a la cola, y el tercero será rechazado
        assertThat(invokeRemoteService(config, 10000), equalTo(null))
        assertThat(invokeRemoteService(config, 10000), equalTo(null))
        assertThat(invokeRemoteService(config, 10000), equalTo(null))

        // Esperamos 5 segundos de modo que pase el tiempo configurado en CircuitBreakerSleepWindowInMilliseconds
        // Y de este modo Hystrix vuelva a cerrar el circuito
        Thread.sleep(5000)

        // Ejecutamos los servicios que ahora no están degradados y responden en el orden de medio segundo.
        assertThat(invokeRemoteService(config, 500), equalTo("Success"))
        assertThat(invokeRemoteService(config, 500), equalTo("Success"))
        assertThat(invokeRemoteService(config, 500), equalTo("Success"))
    }

    fun invokeRemoteService(config: HystrixCommand.Setter?, timeout: Long): String? {
        var response: String? = null
        try {
            response = RemoteServiceTestCommand(
                config,
                RemoteServiceTestSimulator(timeout)
            ).execute()
            println("Remote Service executed in $timeout ms")
        } catch (ex: HystrixRuntimeException) {
            println("ex = $ex")
        }
        return response
    }
}

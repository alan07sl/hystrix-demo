package com.alan07sl.hystrix

/**
 * Empecemos a simular un ejemplo del mundo real
 *
 * En este ejemplo, la clase representa un servicio en un servicio remoto. Tiene un metodo que responde con un mensaje luego de un periodo de tiempo.
 * Ese tiempo podría ser, por ejemplo, el tiempo de respuesta de una API, la cual a su vez podría verse degradada.
 */
class RemoteServiceTestSimulator internal constructor(private val wait: Long) {
    fun execute(): String {
        Thread.sleep(wait)
        return "Success"
    }
}

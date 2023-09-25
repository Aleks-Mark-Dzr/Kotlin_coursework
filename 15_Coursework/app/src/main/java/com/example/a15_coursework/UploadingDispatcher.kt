package com.example.a15_coursework

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalTime

class UploadingDispatcher(private val numPorts: Int) {
    private val job = Job()
    private val busyPorts = Array(numPorts) { false }
    private val busyPortsMutex = Mutex()

    init {
        runBlocking(job) {
            launch { handleTrucks() }
            repeat(numPorts) { portId ->
                launch {
                    while (isActive) {
                        val truck = portQueue.receiveCatching().getOrNull() ?: continue
                        processPort(portId, truck)
                    }
                }
            }
        }
    }

    private suspend fun handleTrucks() {
        while (NonCancellable.isActive) {
            val truck = uploadChannel.receiveCatching().getOrNull() ?: continue
            var sentToPort = false

            busyPortsMutex.withLock {
                for (portId in 0 until numPorts) {
                    if (!busyPorts[portId]) {
                        busyPorts[portId] = true
                        portQueue.send(truck)
                        println("${LocalTime.now()} - Грузовик № ${truck.id} отправлен на порт $portId")
                        sentToPort = true
                        break
                    }
                }
            }

            if (!sentToPort) {
                portQueue.send(truck)
                println("${LocalTime.now()} - Грузовик № ${truck.id} отправлен в очередь")
            }
        }
    }

    private suspend fun processPort(portId: Int, truck: Truck) {
        println("${LocalTime.now()} - Грузовик № ${truck.id}, грузоподъемностью ${truck.loadCapacity} прибыл на порт $portId для разгрузки")
        portMutex.withLock {
            delay(truck.uploadTime.toLong())
            println("${LocalTime.now()} - Грузовик № ${truck.id} разгружен на порту $portId за время ${truck.uploadTime}")
        }
        busyPortsMutex.withLock {
            busyPorts[portId] = false
        }
    }

    fun stop() {
        job.cancel()
    }
}
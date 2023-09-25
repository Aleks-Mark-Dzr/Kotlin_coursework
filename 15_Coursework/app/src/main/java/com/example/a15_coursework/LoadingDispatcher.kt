package com.example.a15_coursework

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalTime

class LoadingDispatcher(private val numPortsLoading: Int) {
    private val job = Job()
    private val busyPorts = Array(numPortsLoading) { false }
    private val busyPortsMutex = Mutex()

    init {
        runBlocking(job) {
            repeat(numPortsLoading) { portId ->
                launch {
                    while (isActive) {
                        val truck = produceTrucksForLoading()
                        processPort(portId, truck)
                    }
                }
            }
        }
    }

    private suspend fun processPort(portId: Int, truck: Truck) {
        println("${LocalTime.now()} - Грузовик № ${truck.id}, грузоподъемностью ${truck.loadCapacity} прибыл на порт B$portId для загрузки")
        portMutex.withLock {
            delay(truck.loadTime.toLong())
            println("${LocalTime.now()} - Грузовик № ${truck.id} загружен на порту B$portId за время ${truck.loadTime}")
        }
        busyPortsMutex.withLock {
            busyPorts[portId] = false
        }
    }

    fun stop() {
        job.cancel()
    }
}
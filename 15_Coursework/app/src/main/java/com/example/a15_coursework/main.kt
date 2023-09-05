package com.example.a15_coursework

import kotlinx.coroutines.*
//import kotlinx.coroutines.DefaultExecutor.isActive
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.sync.Mutex

val uploadChannel = Channel<Truck>()
val portQueue = Channel<Truck>(Channel.UNLIMITED) // Очередь ожидания портов
val portMutex = Mutex() // Мьютекс для контроля доступа к очереди портов

fun generateRandomCargo(truckLoadCapacity: Int): List<Cargo> {
    val cargo = mutableListOf<Cargo>()
    var remainingCapacity = truckLoadCapacity
    val cargoType: String = products.random().type
    var uploadingTime = 0

    if (cargoType == CargoType.FOOD.name) {
        while (remainingCapacity > 0) {
            val randomProduct = products.random()
            if (randomProduct.type == CargoType.FOOD.name) {
                if (randomProduct.weight <= remainingCapacity) {
                    cargo.add(randomProduct)
                    remainingCapacity -= randomProduct.weight
                } else {
                    uploadingTime = cargo.sumBy { uploadingTime }
                    break
                }
            }
        }
        println("Груз: продукты")
    } else {
        while (remainingCapacity > 0) {
            val randomProduct = products.random()
            if (randomProduct.type !== CargoType.FOOD.name) {
                if (randomProduct.weight <= remainingCapacity) {
                    cargo.add(randomProduct)
                    remainingCapacity -= randomProduct.weight
                } else {
                    break
                }
            }
        }
        println("Груз: промтовары")
    }
    println("Тип груза $cargoType")
    println("Груз: $cargo")
    return cargo
}

fun produceTrucks(channel: Channel<Truck>) = runBlocking {
    println("Функция produceTrucks запущена")
    var truckId = 1

    while (true) {
        val truckType = TruckType.values().random()
        val cargo = generateRandomCargo(truckType.loadCapacity)
        val uploadTime = cargo.sumBy { it.uploadingTime }
        val truck = Truck(truckId, truckType.loadCapacity, cargo, uploadTime)
        uploadChannel.send(truck)

        delay(3000) // Ожидание одной секунды перед созданием следующего грузовика
        truckId++
    }
}

fun main() = runBlocking {
    val numPorts = 3
    val producerJob = launch {
        produceTrucks(uploadChannel)
    }

    val dispatcher = launch {
        UploadingDispatcher(numPorts)
    }
}
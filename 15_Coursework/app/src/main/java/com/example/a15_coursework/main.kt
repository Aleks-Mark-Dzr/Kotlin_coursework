package com.example.a15_coursework

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.sync.Mutex

val uploadChannel = Channel<Truck>()
val loadChannel = Channel<Truck>()
val portQueue = Channel<Truck>(Channel.UNLIMITED) // Очередь ожидания портов
val portMutex = Mutex() // Мьютекс для контроля доступа к очереди портов
var truckLoadId = 101

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

fun produceTrucksForUploading(channel: Channel<Truck>) = runBlocking {
    println("Функция produceTrucksForUploading запущена")
    var truckId = 1

    while (true) {
        val truckType = TruckType.values().random()
        val cargo = generateRandomCargo(truckType.loadCapacity)
        val uploadTime = cargo.sumBy { it.uploadingTime }
        val truck = Truck(truckId, truckType.loadCapacity, cargo, uploadTime, loadTime = 0)
        uploadChannel.send(truck)

        delay(3000) // Ожидание одной секунды перед созданием следующего грузовика
        truckId++
    }
}

fun produceTrucksForLoading(): Truck {
    var truckType = TruckType.values().random()
    while (truckType.loadCapacity > 3000){
        truckType = TruckType.values().random()
    }
    val cargo = generateRandomCargo(truckType.loadCapacity)
    val loadTime = cargo.sumBy { it.loadingTime }
    val truck = Truck(truckLoadId, truckType.loadCapacity, cargo, uploadTime = 0, loadTime)

    truckLoadId++
    return truck
}

fun main() = runBlocking {
    val numPortsUploading = 3
    val numPortsLoading = 5
    val producerJob = launch {
        produceTrucksForUploading(uploadChannel)
    }

    val dispatcherUpload = launch {
        UploadingDispatcher(numPortsUploading)
    }

    val dispatcherLoad = launch {
        LoadingDispatcher(numPortsLoading)
    }
}
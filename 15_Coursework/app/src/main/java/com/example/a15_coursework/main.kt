package com.example.a15_coursework

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.time.LocalTime
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


data class Truck(
    val id: Int,
    val loadCapacity: Int,
    val generateRandomCargo: List<Cargo>,
    val uploadTime: Int
)

data class Cargo(
    val type: String,
    val productName: String,
    val weight: Int,
    val loadingTime: Int,
    val uploadingTime: Int
)

enum class TruckType(val loadCapacity: Int) {
    SMALL(1000),
    MEDIUM(3000),
    LARGE(5000)
}

enum class CargoType {
    BULKY,
    MEDIUM,
    SMALL,
    FOOD
}

val products = listOf(
    Cargo(CargoType.BULKY.name, "Холодильник", 70, 100, 180),
    Cargo(CargoType.BULKY.name, "Стиральная машина", 83, 80, 160),
    Cargo(CargoType.BULKY.name, "Духовой шкаф", 42, 80, 60),
    Cargo(CargoType.MEDIUM.name, "Системный блок компьютера", 8, 60, 145),
    Cargo(CargoType.MEDIUM.name, "Монитор", 3, 45, 135),
    Cargo(CargoType.MEDIUM.name, "Принтер", 4, 45, 135),
    Cargo(CargoType.SMALL.name, "Зубная паста_коробка", 1, 35, 130),
    Cargo(CargoType.SMALL.name, "Гель для душа_коробка", 2, 35, 130),
    Cargo(CargoType.SMALL.name, "Шампунь_коробка", 2, 35, 130),
    Cargo(CargoType.FOOD.name, "Хлеб_лоток", 15, 65, 150),
    Cargo(CargoType.FOOD.name, "Молоко_упаковка", 12, 50, 145),
    Cargo(CargoType.FOOD.name, "Картофель_сетка", 5, 55, 150)
)

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
    var truckId = 1

    while (true) {
        val truckType = TruckType.values().random()
        val cargo = generateRandomCargo(truckType.loadCapacity)
        val uploadTime = cargo.sumBy { it.uploadingTime }
        val truck = Truck(truckId, truckType.loadCapacity, cargo, uploadTime)
        channel.send(truck)

        delay(1000) // Ожидание одной секунды перед созданием следующего грузовика
        truckId++
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun main() = runBlocking {
    val uploadChannel = Channel<Truck>(capacity = 3)
    val ports = 3 // Количество доступных портов разгрузки
    val portQueue = Channel<Truck>(Channel.UNLIMITED) // Очередь ожидания портов
    val portMutex = Mutex() // Мьютекс для контроля доступа к очереди портов

    // Запускаем корутины производителя и потребителя
    val producerJob = launch {
        produceTrucks(uploadChannel)
    }

    val consumerJobs = List(ports) { port ->
        launch {

            if (portQueue.isEmpty) {
                for (truck in uploadChannel) {
                    if (uploadChannel.isEmpty) {
                        println("${LocalTime.now()} - Грузовик № ${truck.id}, грузоподъемностью ${truck.loadCapacity} прибыл на порт ${portMutex.withLock { port }} для разгрузки")
                        delay(truck.uploadTime.toLong())
                        println("${LocalTime.now()} - Грузовик № ${truck.id} разгружен на порту ${portMutex.withLock { port }} за время ${truck.uploadTime}")
                    } else {
                        portQueue.send(truck)
                        println("${LocalTime.now()} - Грузовик № ${truck.id} встал в очередь")
                    }
                }
            } else {
                for (truck in portQueue) {
                    val portQ = portQueue.receive()
                    println("${LocalTime.now()} - Грузовик № ${truck.id}, грузоподъемностью ${truck.loadCapacity} прибыл на порт ${portMutex.withLock { port }} для разгрузки из очереди № $portQ")
                    delay(truck.uploadTime.toLong())
                    println("${LocalTime.now()} - Грузовик № ${truck.id} разгружен на порту ${portMutex.withLock { port }} за время ${truck.uploadTime}")
                }
            }
        }
    }

    // Корутина для распределения грузовиков из очереди по доступным портам
    launch {
        for (truck in portQueue) {
            val port = portQueue.receive()
            println("${LocalTime.now()} - Грузовик № ${truck.id}, грузоподъемностью ${truck.loadCapacity} прибыл в очередь № ${port.id} для разгрузки")
        }
    }

    // Ожидание завершения работы всех корутин
    producerJob.join()
    consumerJobs.forEach { it.join() }
}
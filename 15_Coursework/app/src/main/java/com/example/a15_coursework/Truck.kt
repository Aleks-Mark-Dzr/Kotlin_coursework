package com.example.a15_coursework

data class Truck(
    val id: Int,
    val loadCapacity: Int,
    val generateRandomCargo: List<Cargo>,
    val uploadTime: Int,
    val loadTime: Int
)

enum class TruckType(val loadCapacity: Int) {
    SMALL(1000),
    MEDIUM(3000),
    LARGE(5000)
}
package com.example.a15_coursework

data class Cargo(
    val type: String,
    val productName: String,
    val weight: Int,
    val loadingTime: Int,
    val uploadingTime: Int
)

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
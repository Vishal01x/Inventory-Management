package com.example.inventorymanagement.HelperClass

data class inventory (
    val buyersName: String?=null,
    val quantity: Int=0,
    val price: Double=0.0,
    val stockName :String="",
    var image :String?=null,
    val date: Long = System.currentTimeMillis()
)
package com.example.inventorymanagement.HelperClass

data class Sell(
    val buyersName: String?=null,
    val quantity: Int=0,
    val price: Double=0.0,
    val stockName :String=""
)

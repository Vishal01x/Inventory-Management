package com.example.inventorymanagement.HelperClass

data class Stock(
    var id :String? = null,
    val name: String = "",
    var image: String? = null,
    val quantity: Int=0,
    val price: Double=0.0,
    val threshold : Int = 0,
    val date: Long = System.currentTimeMillis()
)
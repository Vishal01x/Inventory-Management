package com.example.inventorymanagement.HelperClass

data class CategoryProducts (
    val name: String = "",
    val image: String? = null,
    val stocks:List<Stock> = emptyList()
)
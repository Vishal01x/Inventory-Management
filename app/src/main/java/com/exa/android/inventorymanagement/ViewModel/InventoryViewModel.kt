package com.example.inventorymanagement.ViewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.inventorymanagement.HelperClass.Category
import com.example.inventorymanagement.HelperClass.Stock
import com.example.inventorymanagement.HelperClass.inventory
import com.example.inventorymanagement.Repository.InventoryRepository

class InventoryViewModel : ViewModel() {
    private val repository = InventoryRepository

    val categories: LiveData<List<Category>> = repository.categories
    val stocks: LiveData<Map<String, List<Stock>>> = repository.stocks
    val inventoryHistory : LiveData<List<inventory>> = repository.inventoryHistory

    val defaultImage:String? =repository.defaultImage

    fun fetchStocks(categoryKey: String) {
        repository.fetchStock(categoryKey)
    }

    fun saveProductInDatabase(data:Stock, category: String, image_uri: Uri?) {
        repository.saveProductInDatabase(data, category, image_uri)
    }
    fun saveProductInRealTime(data: Stock, category: String){
        repository.saveProductInRealTime(data, category)
    }
    fun addInventoryTransectionHistory(inventory: inventory){
        repository.addInventoryTransectionHistory(inventory)
    }
    fun deleteCategory(category: String) {
        repository.deleteCategory(category)
    }
    fun deleteStocks(category: String, stocksId:String) {
        repository.deleteStocks(category, stocksId)
    }
    fun fetchCategoryKey(category: String) : String?{
        return repository.fetchCategoryKey(category)
    }
    fun sellStock(quantity: Int, price: Double,category: String, stockId: String){
        return repository.sellStock(quantity,price,category, stockId)
    }
    fun updateStock(
        name: String = "",
        image: String? = null,
        quantity: Int=0,
        price: Double=0.0,
        category: String,
        stockId: String
    ) {
        return repository.updateStocks(name, image, quantity,price,category, stockId)
    }

    fun buyStock(quantity: Int, price: Double, category: String, stockId: String){
        return repository.buyStock(quantity, price, category, stockId)
    }

}
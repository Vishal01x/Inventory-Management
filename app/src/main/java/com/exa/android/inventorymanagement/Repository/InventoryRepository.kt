package com.example.inventorymanagement.Repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.inventorymanagement.HelperClass.AppConstants
import com.example.inventorymanagement.HelperClass.Category
import com.example.inventorymanagement.HelperClass.Sell
import com.example.inventorymanagement.HelperClass.Stock
import com.example.inventorymanagement.HelperClass.inventory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage

object InventoryRepository {

    private val database = FirebaseDatabase.getInstance()
    private val dataRef = database.reference.child("Category")
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val key = firebaseAuth.currentUser!!.uid
    private val storageRef= Firebase.storage.getReference("Category")
    var defaultImage :String?=null

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> get() = _categories

//    private val _stocks = MutableLiveData<List<Stock>>()
//    val stocks: LiveData<List<Stock>> get() = _stocks
    private val _stocks = MutableLiveData<Map<String, List<Stock>>>()
    val stocks: LiveData<Map<String, List<Stock>>> get() = _stocks

    private val _inventoryHistory = MutableLiveData< List<inventory>>()
    val inventoryHistory: LiveData< List<inventory>> get() = _inventoryHistory

    val categoryKeyMap: MutableMap<String, String> = mutableMapOf()

    init {
        fetchCategories()
        fetchInventoryTransectionHistory()
        fetchDefaultImage(){
            Log.d("default repo ori", defaultImage.toString())
        }
        
    }

    private fun fetchCategories() {
        dataRef.child(key).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                updateCategoriesList()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error\
                Log.d("category", error.message)
            }
        })
    }

    fun fetchCategoryKey( category: String) : String?{
        return categoryKeyMap[category]
    }

    private fun updateCategoriesList() {
        dataRef.child(key).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoriesList = mutableListOf<Category>()
                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.getValue(Category::class.java)
                    category?.let { categoriesList.add(it) }
                    if (category != null) {
                        categoryKeyMap[category.name]=categorySnapshot.key!!
                        fetchStock(category.name)
                    }

                    Log.d("InventoryRepository category", categoryKeyMap.toString())
                }
                _categories.value = categoriesList
                Log.d("InventoryRepository categoryList ", categoriesList.toString())
                Log.d("InventoryRepository categories", categories.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("category update", error.message)
            }
        })
    }

    fun fetchStock(category: String){
        val categoryKey=categoryKeyMap[category]
        if(categoryKey!=null){
            fetchStocksAtKey(categoryKey)
        }else{
            Log.d("InventoryRepository", "category key not found !")
        }
    }

    private fun fetchStocksAtKey(categoryKey: String) {

        Log.d("InventoryRepository", categoryKey)
        if(categoryKey.isEmpty())return
        dataRef.child("$key/$categoryKey").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                updateStocksList(categoryKey)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Log.d("stocks", "${categoryKey}: ${error.message}")
            }
        })
    }

    private fun updateStocksList(categoryKey: String) {
        if(categoryKey.isEmpty() || categoryKey=="defaultImage")return
        Log.d("categoryKey", categoryKey)
        dataRef.child("$key/$categoryKey").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val stocksList = mutableListOf<Stock>()
                for (stockSnapshot in snapshot.children) {
                    if (stockSnapshot.key == "name" || stockSnapshot.key == "image") continue
                    val stock = stockSnapshot.getValue(Stock::class.java)
                    stock?.let { stocksList.add(it) }
                }
                //_stocks.value = stocksList
                val currentStocks = _stocks.value?.toMutableMap() ?: mutableMapOf()
                currentStocks[categoryKey] = stocksList
                _stocks.value = currentStocks
                Log.d("InventoryRepository stocksList", stocksList.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("stocks upadate", "${categoryKey}: ${error.message}")
            }
        })
    }

    fun saveProductInDatabase(data:Stock, category: String, image_uri:Uri?){
        if(category.isEmpty() )return
        val fileReference = storageRef.child("$key/$category/${data.name}.jpg")
        if(image_uri!=null) {
            fileReference.putFile(image_uri).addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener {
                    saveProductInRealTime(data, category)
                }.addOnFailureListener {
                    Log.d("InventoryRepository", "ImageDownload Failed: ${it.message}")
                }
            }.addOnFailureListener{
                Log.d("InventoryRepository", "ImageUpLoad Failed: ${it.message}")
            }
        }else {
            saveProductInRealTime(data, category)
        }
    }
    fun saveProductInRealTime(data: Stock, category: String){
        if(category.isEmpty())return
        dataRef.child("$key/$category").push().setValue(data).addOnSuccessListener {

            Log.d("InventoryRepository","Product is successfully added.")
        }.addOnFailureListener {
            Log.d("InventoryRepository","Product is Failed to add : ${it.message}")
        }
    }
    fun deleteCategory(category: String){
        val categoryKey=categoryKeyMap[category]
        if(categoryKey.isNullOrEmpty())return
        dataRef.child("$key/$categoryKey").removeValue().addOnSuccessListener {
            Log.d("InventoryRepository", "Category successfully deleted.")
        }.addOnFailureListener { error ->
            Log.d("InventoryRepository", "Failed to delete category: ${error.message}")
        }
    }
    fun deleteStocks(category: String, stocksId :String){
        if(category.isEmpty() || stocksId.isEmpty())return
        val categoryKey=categoryKeyMap[category]
        dataRef.child("$key/categories/$categoryKey/$stocksId").removeValue().addOnSuccessListener {
            Log.d("InventoryRepository", "Stocks successfully deleted.")
        }.addOnFailureListener { error ->
            Log.d("InventoryRepository", "Failed to delete stocks: ${error.message}")
        }
    }

//    fun addStockInventory(stockId:String, inventory: inventory){
//        dataRef.child("$key/inventory/$stockId").setValue(inventory).addOnSuccessListener {
//
//            Log.d("InventoryRepository","Inventory is successfully added.")
//        }.addOnFailureListener {
//            Log.d("InventoryRepository","Inventory is Failed to add : ${it.message}")
//        }
//    }
    fun addInventoryTransectionHistory(inventory: inventory){
        dataRef.child("$key/inventory").push().setValue(inventory).addOnSuccessListener {
            Log.d("InventoryRepository","Inventory is successfully added.")
        }.addOnFailureListener {
            Log.d("InventoryRepository","Inventory is Failed to add : ${it.message}")
        }
    }
     private fun fetchDefaultImage(onComplete: ()-> Unit){
        dataRef.child("$key/defaultImage").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var dImage :String?=null
                    for(image in snapshot.children){
                        dImage=image.getValue(String::class.java)

                        Log.d("defaultImage", dImage.toString())
                        if(dImage!=null)defaultImage = dImage
                        Log.d("defaultImage ori", defaultImage.toString())
                    }

                } else {
                    Log.d("defaultImage", "No data found at $key/defaultImage")
                }
                Log.d("defaultImage", defaultImage.toString())
                onComplete()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("defaultImage", "Failed to fetch default image: ${error.message}")
                onComplete()
            }
        })
    }
    private fun fetchInventoryTransectionHistory(){
        dataRef.child("$key/inventory").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val inventoryList:MutableList<inventory> =mutableListOf()
                    for (stockSnapShot in snapshot.children) {
                        val stockInfo=stockSnapShot.getValue(inventory::class.java)
                        if(stockInfo != null){
                            inventoryList.add(stockInfo)
                        }
                    }
                    _inventoryHistory.postValue(inventoryList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("InventoryRepository","fetchInventoryHistory is Failed : ${error.message}")
            }
        })
    }
    fun updateStocks(
        name: String = "",
        image: String? = null,
        quantity: Int=0,
        price: Double=0.0,
        category: String,
        stockId: String
    ){
        val updates = HashMap<String, Any>()
        if(quantity!=0)updates["quantity"] = quantity
        if(price!=0.0)updates["price"] = price
        if(image!=null)updates["image"]=image
        if(name.isNotEmpty())updates["name"]=name
        if(category.isEmpty() || stockId.isEmpty())return
        dataRef.child("$key/categories/$category/$stockId").updateChildren(updates)
            .addOnSuccessListener {
                Log.d("Repo updates", "Updates are successful")
            }
            .addOnFailureListener { e ->
                // Handle any errors
                Log.d("Repo updates", e.message.toString())
            }
    }
    fun sellStock(quantity: Int, price: Double, category: String, stockId: String){
        if(category.isEmpty() || stockId.isEmpty())return
        dataRef.child("$key/categories/$category/$stockId").get().addOnSuccessListener { dataSnapshot ->

            val stock=dataSnapshot.getValue(Stock::class.java)
            val newQuantity=stock?.quantity!! - quantity
            if(newQuantity==0)deleteStocks(category,stockId)
            else updateStocks(stock.name,stock.image, newQuantity,stock.price,category,stock.id!!)

        }.addOnFailureListener {
            Log.w("InventoryRepository sell", "Error updating stock quantity", it)
        }
    }


    fun buyStock(quantity: Int, price: Double, category: String, stockId: String){
        if(category.isEmpty() || stockId.isEmpty())return
        dataRef.child("$key/categories/$category/$stockId").get().addOnSuccessListener { dataSnapshot ->

            val stock=dataSnapshot.getValue(Stock::class.java)
            val newQuantity=stock?.quantity!! + quantity
            val newPrice=calculatePriceAfterBuy(quantity, price, stock.quantity, stock.price)
            if(newQuantity==0)deleteStocks(category,stockId)
            else updateStocks(stock.name,stock.image, newQuantity,newPrice,category,stock.id!!)

        }.addOnFailureListener {
            Log.w("InventoryRepository sell", "Error updating stock quantity", it)
        }
    }

    private fun calculatePriceAfterBuy(buyQuantity: Int, buyPrice: Double, oldQuantity: Int, oldPrice: Double): Double{

        val totalPrice=(buyQuantity*buyPrice)+(oldPrice*oldQuantity)
        val totalQuantity=buyQuantity+oldQuantity
        return totalPrice/totalQuantity
    }
}
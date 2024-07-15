package com.example.inventorymanagement.Fragments

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.exa.android.inventorymanagement.databinding.BuyDialogBinding
import com.exa.android.inventorymanagement.databinding.FragmentStockBinding
import com.exa.android.inventorymanagement.databinding.SellDialogBinding
import com.exa.android.inventorymanagement.databinding.StockDialogBinding
import com.exa.android.inventorymanagement.databinding.StockItemBinding
import com.example.inventorymanagement.Adapter.CategoryViewHolder
import com.example.inventorymanagement.Adapter.StockViewHolder
import com.example.inventorymanagement.HelperClass.Category
import com.example.inventorymanagement.HelperClass.IdManager
import com.example.inventorymanagement.HelperClass.Sell
import com.example.inventorymanagement.HelperClass.Stock
import com.example.inventorymanagement.HelperClass.inventory
import com.example.inventorymanagement.ViewModel.InventoryViewModel
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage

class StockFragment : Fragment() {

    private var _binding : FragmentStockBinding?=null
    private val binding get() = _binding!!
    private  var categorykey :String? = null
    private var category: String?=null
    private lateinit var database: FirebaseDatabase
    private lateinit var dataRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var key:String
    private lateinit var storageRef: StorageReference
    private var image_uri  : Uri? = null
    private val inventoryViewModel: InventoryViewModel by activityViewModels()

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()){
        image_uri = it
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding=FragmentStockBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        category = arguments?.getString("category")

        binding.toolbar.title = category

        Log.d("StockFragment category", category.toString())
        database=FirebaseDatabase.getInstance()
        dataRef=database.reference.child("Category")
        firebaseAuth= FirebaseAuth.getInstance()
        key=firebaseAuth.currentUser!!.uid
        storageRef= Firebase.storage.getReference("Category")

        binding.addStockFab.setOnClickListener{
            showStockDialog()
        }
        Log.d("default repo stockFragment", inventoryViewModel.defaultImage.toString())

        category?.let { loadStocks(it) }

    }
    private lateinit var adapter:FirebaseRecyclerAdapter<Stock, StockViewHolder>
    private  fun loadStocks(categoryKey:String){
        val query = dataRef.child("$key/categories/$category")
        val option= FirebaseRecyclerOptions.Builder<Stock>()
            .setQuery(query, Stock::class.java)
            .build()
        adapter = object : FirebaseRecyclerAdapter<Stock, StockViewHolder>(option) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
                val binding=
                    StockItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return StockViewHolder(binding, parent.context)
            }

            override fun onBindViewHolder(holder: StockViewHolder, position: Int, model: Stock) {
                val item=getItem(position)
                holder.bind(item)
                holder.binding.sellButton.setOnClickListener {
                    onClickSell(model)
                }
                holder.binding.edit.setOnClickListener{
                    onClickEdit(model)
                }
                holder.binding.delete.setOnClickListener{
                    onClickDelete(model)
                }
                holder.binding.buyButton.setOnClickListener {
                    onClickBuy(model)
                }
            }
        }
        binding.stockRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.stockRecycler.adapter=adapter
        adapter.startListening()
    }
    private fun saveProductInDatabase(data:Stock){

        val fileReference = storageRef.child("$key/$category/${data.name}.jpg")
        if(image_uri!=null) {
            fileReference.putFile(image_uri!!).addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener {
                    saveProductInRealTime(data)
                }.addOnFailureListener {
                    Log.d("StockFragment", "ImageDownload Failed: ${it.message}")
                }
            }.addOnFailureListener{
                Log.d("StockFragment", "ImageUpLoad Failed: ${it.message}")
            }
        }else {
            saveProductInRealTime(data)
        }
    }
    private fun saveProductInRealTime(data: Stock){

        val ref=dataRef.child("$key/categories/$category").push()
        val dataKey=ref.key
        data.id=dataKey!!
        ref.setValue(data).addOnSuccessListener {

            Log.d("StockFragment","Product is successfully added:$data")
        }.addOnFailureListener {
            Log.d("StockFragment","Product is Failed to add : ${it.message}")
        }
    }
    private fun showStockDialog(){

        val sBinding = StockDialogBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add New Stock")
            .setView(sBinding.root)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }.create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(Dialog.BUTTON_POSITIVE)

            sBinding.pickImageButton.setOnClickListener{
                getContent.launch("image/*")
            }

            btn.setOnClickListener {
                val name = sBinding.stockName.text.toString()
                val quantity = sBinding.quantity.text.toString()
                val price = sBinding.price.text.toString()
                val threshold = sBinding.threashold.text.toString()
                if (name.isEmpty()) {
                    sBinding.stockName.error = "Name is Required"
                } else if (quantity.isEmpty()) {
                    sBinding.quantity.error = "Quantity is Required"
                } else if (price.isEmpty()) {
                    sBinding.price.error = "Price is Required"
                } else if(threshold.isEmpty()){
                  sBinding.threashold.error = "Threshold is Required"
            }else {
                    val quantity = sBinding.quantity.text.toString().toIntOrNull() ?: 0
                    val price = sBinding.price.text.toString().toDoubleOrNull() ?: 0.0
                    val name = sBinding.stockName.text.toString()
                    val threshold_value = sBinding.threashold.text.toString().toInt()
                    val inventory=inventory(
                        quantity=quantity,
                        price = price,
                        stockName = name,
                        image = image_uri.toString()
                    )
                    if(image_uri.toString()=="null")inventory.image=inventoryViewModel.defaultImage
                    val data= Stock(
                        name= name,
                        image = image_uri.toString(),
                        quantity=quantity,
                        price=price,
                        threshold = threshold_value
                    )
                    if(image_uri.toString()=="null")data.image=inventoryViewModel.defaultImage
                    Log.d("StockFragment data", data.toString())
                    Log.d("StockFragment inventory", inventoryViewModel.defaultImage.toString())
                    Log.d("StockFragment image_uri", image_uri.toString())
                    saveProductInDatabase(data)
                    inventoryViewModel.addInventoryTransectionHistory(inventory)
                    image_uri = null
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    fun onClickEdit(stock: Stock){
        val sBinding = StockDialogBinding.inflate(layoutInflater)
        sBinding.stockName.text = Editable.Factory.getInstance().newEditable(stock.name)
        sBinding.price.text = Editable.Factory.getInstance().newEditable(stock.price.toString())
        sBinding.quantity.text = Editable.Factory.getInstance().newEditable(stock.quantity.toString())

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Stock")
            .setView(sBinding.root)
            .setPositiveButton("Edit"){dialog,_ ->
                val quantity = sBinding.quantity.text.toString().toIntOrNull() ?: 0
                val price = sBinding.price.text.toString().toDoubleOrNull() ?: 0.0
                val name = sBinding.stockName.text.toString()
                val image=null
                val inventory=inventory(
                    quantity=quantity,
                    price = price,
                    stockName = name,
                    image = image_uri.toString()
                )
                val data= Stock(
                    name= name,
                    image = image_uri.toString(),
                    quantity=quantity,
                    price=price
                )

                inventoryViewModel.updateStock(name,image,quantity,price,category!!,stock.id!!)
                inventoryViewModel.addInventoryTransectionHistory(inventory)
              // add delete this data from daotabase and then add to database
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    fun onClickDelete(stock : Stock){
        // delete this stock from database
        inventoryViewModel.deleteStocks(category!!, stock.id!!)
    }

    fun onClickSell(stock : Stock){
        val sBinding = SellDialogBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sell Stock")
            .setView(sBinding.root)
            .setPositiveButton("Sell", null)
            .setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }.create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(Dialog.BUTTON_POSITIVE)

            btn.setOnClickListener {
                val name = sBinding.buyerName.text.toString()
                val quantity = sBinding.quantity.text.toString()
                val price = sBinding.price.text.toString()
                if (name.isEmpty()) {
                    sBinding.buyerName.error = "Name is Required"
                } else if (quantity.isEmpty()) {
                    sBinding.quantity.error = "Quantity is Required"
                } else if (price.isEmpty()) {
                    sBinding.price.error = "Price is Required"
                } else {

                    if (quantity.toInt() > stock.quantity.toInt()) {
                        sBinding.quantity.error = "Quantity is Limited"
                    } else {
                        val quantity = sBinding.quantity.text.toString().toIntOrNull() ?: 0
                        val price = sBinding.price.text.toString().toDoubleOrNull() ?: 0.0
                        val buyersName = sBinding.buyerName.text.toString()
                        val data = inventory(
                            buyersName = buyersName,
                            quantity = quantity,
                            price = price,
                            stockName =stock.name,
                            image = stock.image
                        )

                        inventoryViewModel.sellStock(quantity, price,category!!, stock.id!!)
                        // now add this sell to database
                        inventoryViewModel.addInventoryTransectionHistory(data)

                        val newQuantity = stock.quantity.toInt() - quantity.toInt()
                        if(newQuantity == 0){
                            onClickDelete(stock) // use to delete stock
                        }else{
                            // delete this stock from database and add updated stock
                        }

                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    fun onClickBuy(stock: Stock){
        val sBinding = BuyDialogBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Buy Stock")
            .setView(sBinding.root)
            .setPositiveButton("Buy", null)
            .setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }.create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(Dialog.BUTTON_POSITIVE)

            btn.setOnClickListener {
                val quantity = sBinding.quantity.text.toString()
                val price = sBinding.price.text.toString()
                 if (quantity.isEmpty()) {
                    sBinding.quantity.error = "Quantity is Required"
                } else if (price.isEmpty()) {
                    sBinding.price.error = "Price is Required"
                } else {
                        val quantity = sBinding.quantity.text.toString().toIntOrNull() ?: 0
                        val price = sBinding.price.text.toString().toDoubleOrNull() ?: 0.0
                        val data = inventory(
                            buyersName = "",
                            quantity = quantity,
                            price = price,
                            stockName =stock.name,
                            image = stock.image
                        )

                        inventoryViewModel.buyStock(quantity, price,category!!, stock.id!!)
                        // now add this sell to database
                        inventoryViewModel.addInventoryTransectionHistory(data)


                        dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
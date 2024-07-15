package com.example.inventorymanagement.Fragments

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.exa.android.inventorymanagement.R
import com.exa.android.inventorymanagement.databinding.FragmentHomeBinding
import com.example.inventorymanagement.Adapter.CategoryViewHolder
import com.example.inventorymanagement.HelperClass.Category
import com.example.inventorymanagement.HelperClass.CustomBarChartRenderer
import com.example.inventorymanagement.HelperClass.Stock
import com.example.inventorymanagement.ViewModel.InventoryViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.charts.PieChart
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage

class HomeFragment : Fragment() {

    private var _binding:FragmentHomeBinding ?=null
    private val binding get() = _binding!!
    private lateinit var bar :BarChart
    private lateinit var pie:PieChart
    private lateinit var database:FirebaseDatabase
    private lateinit var dataRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private lateinit var key:String
    private val inventoryViewModel: InventoryViewModel by activityViewModels()
    private lateinit var categoryImage: Uri
    private var isImageAdded=false
    private var stockFragment=StockFragment()
    private var barEntries = mutableListOf<BarEntry>()
    private var labels = mutableListOf<String>()

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()){uri->

        uri?.let {
            categoryImage=it
            isImageAdded=true
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding= FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database=FirebaseDatabase.getInstance()
        dataRef=database.reference.child("Category")
        firebaseAuth= FirebaseAuth.getInstance()
        storageRef= Firebase.storage.getReference("Category")
        key=firebaseAuth.currentUser!!.uid

        binding.fab.setOnClickListener{
            showCategoryDialog()
        }
        setUpPieChart()
        loadCategory()
        fetchStockForBarChart()
//        inventoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
//            // Update your UI with the categories
//            Log.d("HomeFragment1", categories.toString())
//        }
//
//        inventoryViewModel.stocks.observe(viewLifecycleOwner) { stocks ->
//            // Update your UI with the stocks
//            Log.d("HomeFragment2", stocks.toString())
//        }
    }
    /*
    private fun uploadDefaultImages(){

        //showCategoryDialog()
        if(isImageAdded){
            storageRef.child("$key/defaultImage.jpg").putFile(categoryImage).addOnSuccessListener {

                storageRef.child("$key/defaultImage.jpg").downloadUrl.addOnSuccessListener { uri ->

                    dataRef.child("$key/defaultImage").push().setValue(uri.toString()).addOnSuccessListener {
                        Log.d("StockFragment","Data successfully saved")
                    }.addOnFailureListener {
                        Log.d("StockFragment","Error Failed to saved: ${it.message}")
                    }

                }.addOnFailureListener {
                    Log.d("HomeFragment","Error Failed to saved: ${it.message}")
                }
            }
        }
    }
    */
    private fun showCategoryDialog(){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.category_layout, null)
        val edittxt = dialogView.findViewById<EditText>(R.id.edit)
        val insert_img = dialogView.findViewById<Button>(R.id.btn)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add New Category")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            insert_img.setOnClickListener {
                getContent.launch("image/*")
            }
            btn.setOnClickListener {
                val category = edittxt.text.toString()
                if(category.isNotEmpty()){
                    if(isImageAdded){
                        storeCategoryInStorage(category)

                    }
                    else{
                        val categoryInfo=Category(
                            name = category,
                            image = inventoryViewModel.defaultImage
                        )
                        storeCategoryInDatabase(categoryInfo)
                    }
                    sendArg(category)
                    dialog.dismiss()
                    parentFragmentManager.beginTransaction().replace(R.id.fragment_container_view, stockFragment)
                        .addToBackStack("home fragment")
                        .commit()
                }else{
                    edittxt.error = "Category Name Cannot Be Empty"
                }
            }
        }
        dialog.show()
    }

    private fun storeCategoryInStorage(category:String) {
        storageRef.child("$key/$category.jpg").putFile(categoryImage).addOnSuccessListener {

            storageRef.child("$key/$category.jpg").downloadUrl.addOnSuccessListener { uri ->

                val categoryInfo=Category(
                    name = category,
                    image = uri.toString()
                )
                storeCategoryInDatabase(categoryInfo)

            }.addOnFailureListener {
                Log.d("HomeFragment","Error Failed to saved: ${it.message}")
            }
        }
    }

    private fun storeCategoryInDatabase(categoryInfo: Category) {

        dataRef.child("$key/${categoryInfo.name}").setValue(categoryInfo).addOnSuccessListener {
            Log.d("StockFragment","Data successfully saved")
        }.addOnFailureListener {
            Log.d("StockFragment","Error Failed to saved: ${it.message}")
        }
    }

    private fun setUpPieChart(){
        val data=listOf<PieEntry>(
            PieEntry(50f, "Sell"),
            PieEntry(40f, "Buy"),
            PieEntry(10f, "Profit")
        )
        val dataSet=PieDataSet(data, "Profit-Loss Report")
        dataSet.colors=ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.valueTextColor=Color.BLACK
        dataSet.valueTextSize=15f
        val pieData=PieData(dataSet)
//        binding.piechart.apply{
//            this.data=pieData
//            this.description.isEnabled=true
//            this.centerText="Profit / Loss"
//            this.animate()
//        }
        pie= binding.piechart
        pie.apply {
            this.data = pieData
            this.description.isEnabled = true
            this.centerText = "Profit / Loss"
            this.setUsePercentValues(true) // Use percentage values
            this.setDrawEntryLabels(false) // Disable drawing entry labels
            this.setDrawCenterText(true)
            this.animate()

            // Customize label text properties
            this.setDrawEntryLabels(true)
            this.setEntryLabelColor(Color.BLACK)
            this.setEntryLabelTextSize(12f)

        }
    }
    private fun fetchStockForBarChart(){
        Log.d("FetchStockForBarChart", "snapshot1")
        dataRef.child("$key/categories").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FetchStockForBarChart", "snapshot2")
                if(snapshot.exists()){
                    val newBarEntries = mutableListOf<BarEntry>()
                    val newLabels = mutableListOf<String>()
                    var index = 1f
                    for(categorySnapshot in snapshot.children) {
                        Log.d("FetchStockForBarChart parent", categorySnapshot.key.toString())
                        for (dataSnapshot in categorySnapshot.children) {
                            val stock = dataSnapshot.getValue(Stock::class.java)
                            Log.d("FetchStockForBarChart child", dataSnapshot.key.toString())
                            Log.d("FetchStockForBarChart stock",stock.toString())
                            if (stock != null) {
                                newBarEntries.add(BarEntry(index,stock.quantity.toFloat()))
                                index += 1f
                                newLabels.add(stock.name)
                            }
                        }
                    }
                    barEntries= (barEntries+newBarEntries).toSet().toMutableList()
                    labels=(labels+newLabels).toSet().toMutableList()
                    Log.d("barEntries",barEntries.toString())
                    Log.d("labels", labels.toString())
                    setUpBarChart()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("HomeFragment","Error Failed to saved: ${error.message}")
            }
        })
    }
    private fun setUpBarChart(){
        if (!::bar.isInitialized) {
            bar = binding.barchart
        }
        var wasEmpty=false
        if(barEntries.isNullOrEmpty() || labels.isNullOrEmpty()) {
            wasEmpty=true
            barEntries = listOf<BarEntry>(
                BarEntry(0f, 70f), // Add text as the third parameter
                BarEntry(1f, 30f),
                BarEntry(2f, 100f),
                BarEntry(3f, 60f)
            ).toMutableList()
            labels = listOf("Text1", "Text2", "Text3", "Text4").toMutableList()
        }
        Log.d("setUpBarChart", barEntries.toString())
        Log.d("setUpBarChart", labels.toString())
        val dataset=BarDataSet(barEntries, "Report")
        dataset.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataset.valueTextColor= Color.BLACK
        dataset.valueTextSize=15f

        val barData=BarData(dataset)

        bar.setFitBars(true)
        bar.data=barData
        bar.description.text="Bar Report"
        bar.animateY(2000)
//        bar.renderer = CustomBarChartRenderer(
//                bar,
//                bar.animator,
//                bar.viewPortHandler,
//                labels
//        )

        if(wasEmpty){
            barEntries= mutableListOf()
            labels= mutableListOf()
        }


    }
    private lateinit var adapter: FirebaseRecyclerAdapter<Category, CategoryViewHolder>
    private  fun loadCategory(){
        val query = dataRef.child(key)
        val option= FirebaseRecyclerOptions.Builder<Category>()
            .setQuery(query, Category::class.java)
            .build()
        adapter = object : FirebaseRecyclerAdapter<Category, CategoryViewHolder>(option) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
                val view=LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
                return CategoryViewHolder(view)
            }

            override fun onBindViewHolder(holder: CategoryViewHolder, position: Int, model: Category) {
                if(model.name.isNotEmpty()) {
                    holder.categoryView.text = model.name
                    if(model.image?.isNotEmpty() == true) {
                        Glide.with(holder.categoryImage.context)
                            .load(model.image)
                            .into(holder.categoryImage)
                    }
                    holder.view.setOnClickListener {
                        val categoryKey = inventoryViewModel.fetchCategoryKey(model.name)

                        sendArg(categoryKey)

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container_view, stockFragment)
                            .addToBackStack("homefragment")
                            .commit()
                    }
                }else holder.view.visibility = View.INVISIBLE
            }
        }
        binding.categoryRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.categoryRecycler.adapter=adapter
        adapter.startListening()
    }
    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
    /*
//    private fun fetchCategorieDetails(){
//        dataRef.child(key).addValueEventListener(object :ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//                if(snapshot.exists()){
//                    for(categorySnapshot in snapshot.children){
//                        Log.d("category key", categorySnapshot.key.toString())
//                        val category = categorySnapshot.getValue(CategoryProducts::class.java)
//                        category?.let {
//                             Log.d("category", "Name: ${it.name}, Image: ${it.image}")
//                        }
//                        if (category != null) {
//                            dataRef.child("$key/${categorySnapshot.key}").addValueEventListener(object: ValueEventListener{
//                                override fun onDataChange(snapshotChild: DataSnapshot) {
//                                    for (itemSnapshot in snapshotChild.children) {
//                                        if (itemSnapshot.key == "name" || itemSnapshot.key == "image") continue
//
//                                        val stock = itemSnapshot.getValue(Stock::class.java)
//                                        if (stock != null) {
//                                            Log.d("category", "${category.name}, Name: ${stock.name}, Quantity: ${stock.quantity}, Price: ${stock.price}, Image: ${stock.image}")
//                                        }
//                                    }
//                                }
//
//                                override fun onCancelled(error: DatabaseError) {
//                                    Log.d("category", "${category.name}: ${error.message}")
//                                }
//                            })
//                        }
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.d("category", error.message)
//                AppConstants.showToast(requireContext(),"Error Fetching data: ${error.message}")
//            }
//        })
//    }

     */

    fun sendArg(name : String?) {
        if (name != null) {


            val bundle = Bundle().apply {
                putString("category", name)
            }
            stockFragment.arguments = bundle
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
/*
//                for(category in snapshot.children){
//                    Log.d("category whole array", category.toString())
//                    val category = snapshot.getValue(CategoryProducts::class.java)
////                    category?.inventory.let { Log.d("category inventory", it.toString()) }
////                    category?.category?.name?.let { Log.d("category name", it) }
////                    category?.category?.image?.let { Log.d("category image",it) }
//                    category?.image?.let { Log.d("category image", it) }
//                    category?.name?.let{ Log.d("category name", it) }
//                    category?.inventory?.let { Log.d("category inventory", it.toString()) }
//                    Log.d("category", category.toString())
//                }
//                Log.d("category4", snapshot.toString())
//                for (categorySnapshot in snapshot.children) {
////                    val category = categorySnapshot.getValue(CategoryProducts::class.java)
////                    category?.let {
////                        Log.d("category", "Name: ${it.name}, Image: ${it.image}")
////                        val stock = it.getValue(Stock::class.java)
////                        for(item in it.items){
////                            Log.d("category", " Inventory: ${it.get}")
////                        }
////                    }
////                    Log.d("category", categorySnapshot.toString())
//                    val category = Category(
//                        name = categorySnapshot.child("name").getValue(String::class.java) ?: "",
//                        image = categorySnapshot.child("image").getValue(String::class.java) ?: ""
//                    )
//                    Log.d("category", category.toString())
//                    val name = categorySnapshot.child("inventory").getValue(inventory::class.java) ?: ""
//                    val image = categorySnapshot.child("sells").getValue(Sell::class.java) ?: ""
//                    Log.d("category 0", name.toString())
//                    Log.d("category 0", image.toString())
//                    for (itemSnapshot in categorySnapshot.children) {
////                        // Skip the "name" and "image" fields
//                        Log.d("category 3", itemSnapshot.toString())
//                        if (itemSnapshot.key.toString() == "name" || itemSnapshot.key.toString() == "image"){
//                            val name = itemSnapshot.child("inventory").getValue(inventory::class.java) ?: ""
//                            val image = itemSnapshot.child("sells").getValue(Sell::class.java) ?: ""
//                            Log.d("category 2", name.toString())
//                            Log.d("category 2", image.toString())
//                        }
//                        Log.d("category 1", itemSnapshot.key.toString())
////
////                        // Get the Stock object
//                        //val stock = itemSnapshot.getValue(Stock::class.java)
////                        Log.d("category", stock.toString())
//                    }
//                }
*/

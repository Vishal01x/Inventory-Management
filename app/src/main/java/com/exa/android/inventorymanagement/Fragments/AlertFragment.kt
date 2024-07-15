package com.exa.android.inventorymanagement.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.exa.android.inventorymanagement.databinding.FragmentAlertBinding
import com.example.inventorymanagement.Adapter.AlertAdapter
import com.example.inventorymanagement.HelperClass.Stock
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AlertFragment : Fragment() {

    private var _binding: FragmentAlertBinding?=null
    private val binding get() = _binding!!
    private var stocks = mutableListOf<Stock>()
    private lateinit var database: FirebaseDatabase
    private lateinit var dataRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var key:String
    private lateinit var recycleview : RecyclerView
    private lateinit var adapter : AlertAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding=FragmentAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = "Low Stock Alert"

        recycleview = binding.alertRecycler
        adapter = AlertAdapter()
        recycleview.adapter = adapter
        recycleview.layoutManager = LinearLayoutManager(requireContext())

        database=FirebaseDatabase.getInstance()
        dataRef=database.reference.child("Category")
        firebaseAuth= FirebaseAuth.getInstance()
        key=firebaseAuth.currentUser!!.uid
        loadStockData()
    }
    fun loadStockData(){
        dataRef.child("$key/categories").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val newStocks = mutableListOf<Stock>()
                    for(categorySnapshot in snapshot.children) {
                        for (dataSnapshot in categorySnapshot.children) {
                            val stock = dataSnapshot.getValue(Stock::class.java)
                            if (stock != null && stock.quantity<stock.threshold) {
                                newStocks.add(stock)
                            }
                        }
                    }
                    stocks=(stocks+newStocks).toMutableList()
                    adapter.submitList(stocks)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("HomeFragment","Error Failed to saved: ${error.message}")
            }
        })
    }
}
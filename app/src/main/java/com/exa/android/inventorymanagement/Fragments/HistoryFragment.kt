package com.example.inventorymanagement.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.exa.android.inventorymanagement.databinding.FragmentHistoryBinding
import com.exa.android.inventorymanagement.databinding.InventoryItemBinding
import com.example.inventorymanagement.Adapter.InventoryViewHolder
import com.example.inventorymanagement.Adapter.StockViewHolder
import com.example.inventorymanagement.HelperClass.Stock
import com.example.inventorymanagement.HelperClass.inventory
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding?=null
    private val binding get() =_binding!!
    private lateinit var database: FirebaseDatabase
    private lateinit var dataRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var key:String
    private lateinit var adapter: FirebaseRecyclerAdapter<inventory, InventoryViewHolder>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding=FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = "Transaction"

        database=FirebaseDatabase.getInstance()
        dataRef=database.reference.child("Category")
        firebaseAuth= FirebaseAuth.getInstance()
        key=firebaseAuth.currentUser!!.uid

        loadStocks()

    }
    private  fun loadStocks(){
        val query = dataRef.child("$key/inventory")
        val option= FirebaseRecyclerOptions.Builder<inventory>()
            .setQuery(query, inventory::class.java)
            .build()
        adapter = object : FirebaseRecyclerAdapter<inventory, InventoryViewHolder>(option) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
                val binding= InventoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return InventoryViewHolder(binding, parent.context)
            }

            override fun onBindViewHolder(holder: InventoryViewHolder, position: Int, model: inventory) {
                val item=getItem(position)
                holder.bind(item)
            }
        }
        binding.inventoryRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.inventoryRecycler.adapter=adapter
        adapter.startListening()
    }
}
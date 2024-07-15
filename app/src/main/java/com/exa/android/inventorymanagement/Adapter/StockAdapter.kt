package com.example.inventorymanagement.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.exa.android.inventorymanagement.databinding.StockItemBinding
import com.example.inventorymanagement.HelperClass.Category
import com.example.inventorymanagement.HelperClass.Stock


class StockAdapter : ListAdapter<Stock, StockAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StockItemBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(binding,parent.context)
    }

    override fun onBindViewHolder(holder: StockAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder(private val binding: StockItemBinding, val  context : Context) : RecyclerView.ViewHolder(binding.root){
        fun bind(stock : Stock){
           binding.stockName.text = stock.name
            binding.price.text = stock.price.toString()
            binding.quantity.text = stock.quantity.toString()
            Glide.with(context).load(stock.image).into(binding.pickedImg)
        }
    }

    class Diff : DiffUtil.ItemCallback<Stock>(){
        override fun areItemsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            return oldItem == newItem
        }


    }

}
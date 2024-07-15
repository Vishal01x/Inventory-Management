package com.example.inventorymanagement.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.exa.android.inventorymanagement.R
import com.exa.android.inventorymanagement.databinding.AlertItemBinding
import com.example.inventorymanagement.HelperClass.Category
import com.example.inventorymanagement.HelperClass.Stock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlertAdapter : ListAdapter<Stock, AlertAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AlertItemBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(binding,parent.context)
    }

    override fun onBindViewHolder(holder: AlertAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder(private val binding: AlertItemBinding, val  context : Context) : RecyclerView.ViewHolder(binding.root){
        fun bind(stock : Stock){
            binding.stockName.text = stock.name
            binding.price.text = stock.price.toString()
            binding.quantity.text = stock.quantity.toString()
            if(stock.image==null){
                binding.pickedImg.setImageResource(R.drawable.default_img)
            }
            else {
                Glide.with(context).load(stock.image).into(binding.pickedImg)
            }

            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(stock.date))

            binding.date.text = formattedDate
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
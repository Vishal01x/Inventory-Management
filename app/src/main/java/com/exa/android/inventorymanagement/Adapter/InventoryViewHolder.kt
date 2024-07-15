package com.example.inventorymanagement.Adapter

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.exa.android.inventorymanagement.R
import com.exa.android.inventorymanagement.databinding.InventoryItemBinding
import com.example.inventorymanagement.HelperClass.Stock
import com.example.inventorymanagement.HelperClass.inventory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryViewHolder(val binding: InventoryItemBinding, val  context : Context) : RecyclerView.ViewHolder(binding.root) {

    fun bind(inventory: inventory){
        binding.stockName.text=inventory.stockName
        binding.price.text = inventory.price.toString()
        binding.quantity.text = inventory.quantity.toString()

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(inventory.date))

        binding.date.text = formattedDate.toString()

        if(!inventory.buyersName.isNullOrEmpty()) {
            val errorColor = ContextCompat.getColor(context, R.color.errorColor)
            binding.price.setTextColor(errorColor)
            binding.priceText.setTextColor(errorColor)
            binding.buyerName.text = inventory.buyersName
            binding.buyerView.visibility= View.VISIBLE
        }else{
            binding.buyerView.visibility = View.GONE
        }
        if(inventory.image==null){
            binding.pickedImg.setImageResource(R.drawable.default_img)
        }
        else {
            Glide.with(context).load(inventory.image).into(binding.pickedImg)
        }
    }
}
package com.example.inventorymanagement.Adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.exa.android.inventorymanagement.R


class CategoryViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
    val categoryView=itemView.findViewById<TextView>(R.id.category_name)
    val categoryImage=itemView.findViewById<ImageView>(R.id.category_image)
    val view=itemView
}
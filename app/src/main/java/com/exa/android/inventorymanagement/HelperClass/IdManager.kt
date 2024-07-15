package com.example.inventorymanagement.HelperClass

import android.content.Context
import android.content.SharedPreferences

object IdManager {
    private const val PREFS_NAME = "stock_prefs"
    private const val KEY_CURRENT_ID = "current_id"

    fun getNextId(context: Context, category: String): Int {
//        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        val currentId = sharedPreferences.getInt(KEY_CURRENT_ID, 0)
//        sharedPreferences.edit().putInt(KEY_CURRENT_ID, currentId + 1).apply()
//        return currentId
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = "$category$KEY_CURRENT_ID"
        val currentId = sharedPreferences.getInt(key, 0)
        sharedPreferences.edit().putInt(key, currentId + 1).apply()
        return currentId
    }
}

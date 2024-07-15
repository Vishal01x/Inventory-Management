package com.example.inventorymanagement.HelperClass

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import com.exa.android.inventorymanagement.MainActivity

object AppConstants {
    val TAG= MainActivity::class.simpleName
    fun verifyEmail(email:String):Pair<Boolean, String>{
        var result=Pair(true, "")
        if(email.isBlank()){
            result=Pair(false, "Email must be provided.")
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.contains('@')){
            result=Pair(false, "Enter valid Email")
        }
        return result
    }
    fun verifyPassword(password:String):Pair<Boolean, String>{
        var result=Pair(true, "")
        if(password.isBlank()){
            result=Pair(false, "Password must be provided.")
        }else if(password.length<6){
            result=Pair(false, "Password must be of 6 character")
        }
        return result
    }
    fun verifyConfirmPassword(password:String, confirmPassword:String):Pair<Boolean, String>{
        var result=Pair(true, "")
        if(password.isBlank() || confirmPassword.isBlank())result=Pair(false, "Password must be provided.")
        else if(password!=confirmPassword)result=Pair(false, "ConfirmPassword must match with Password")
        return result
    }
    fun showToast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
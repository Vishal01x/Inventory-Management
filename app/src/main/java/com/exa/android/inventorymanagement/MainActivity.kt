package com.exa.android.inventorymanagement

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.exa.android.inventorymanagement.Fragments.AlertFragment
import com.exa.android.inventorymanagement.Fragments.Authentication.SignUpFragment
import com.exa.android.inventorymanagement.databinding.ActivityMainBinding
import com.example.inventorymanagement.Fragments.HistoryFragment
import com.example.inventorymanagement.Fragments.HomeFragment


class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding?=null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(SignUpFragment())
        }

        binding.btmNaviView.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.home -> HomeFragment()
                R.id.alert -> AlertFragment()
                R.id.history -> HistoryFragment()
                else -> HomeFragment()
            }

            loadFragment(fragment)
            true
        }
    }
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}
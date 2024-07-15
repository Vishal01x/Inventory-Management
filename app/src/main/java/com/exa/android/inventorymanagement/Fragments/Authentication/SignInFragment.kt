package com.example.inventorymanagement.Fragments.Authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.exa.android.inventorymanagement.R
import com.exa.android.inventorymanagement.Fragments.Authentication.SignUpFragment
import com.exa.android.inventorymanagement.databinding.FragmentSignInBinding
import com.example.inventorymanagement.Fragments.HomeFragment
import com.example.inventorymanagement.HelperClass.AppConstants
import com.google.firebase.auth.FirebaseAuth

class SignInFragment : Fragment() {

    private var _binding : FragmentSignInBinding?=null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding=FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth= FirebaseAuth.getInstance()
        binding.btnLogin.setOnClickListener {
            signInWithEmail()
        }
        binding.signUpNavigation.setOnClickListener {
            // navigate to sign up
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container_view, SignUpFragment()).commit()
        }
    }

    private fun signInWithEmail() {
        val email=binding.emailAddressEt.text.toString()
        val password=binding.passwordEt.text.toString()

        val verifyEmail= AppConstants.verifyEmail(email)
        val verifyPassword= AppConstants.verifyPassword(password)
        if(!verifyEmail.first and !verifyPassword.first){
            binding.emailTextInputLayout.error=verifyEmail.second.toString()
            binding.passwordTextInputLayout.error=verifyPassword.second.toString()
            return
        }else if(!verifyEmail.first){
            binding.emailTextInputLayout.error=verifyEmail.second
            return
        }else if(!verifyPassword.first){
            binding.passwordTextInputLayout.error=verifyPassword.second
            return
        }
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    // navigate to home frament
                    parentFragmentManager.beginTransaction().replace(R.id.fragment_container_view, HomeFragment()).commit()
                }else{
                    Toast.makeText(requireContext(), "Error creating user: ${it.exception?.message.toString()}", Toast.LENGTH_SHORT).show()
                }
            }

    }
    override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null ) {
            // redirect to the home fragment
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container_view, HomeFragment()).commit()

        }
    }
}
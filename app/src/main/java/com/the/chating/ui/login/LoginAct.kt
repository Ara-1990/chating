package com.the.chating.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.app.ProgressDialog
import android.content.Intent
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.the.chating.MainActivity
import com.the.chating.databinding.ActivityLoginBinding
import com.the.chating.ui.register.RegisterAct
import com.the.chating.ui.register.Resource
import kotlinx.coroutines.launch


class LoginAct : AppCompatActivity() {


    private lateinit var binding: ActivityLoginBinding

    private lateinit var viewModel: LoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]


        binding.noAccountTv.setOnClickListener {

            startActivity(Intent(this, RegisterAct::class.java))
        }

        binding.loginBtn.setOnClickListener {
            validateAndLogin()
        }

        observeState()
    }

    private fun validateAndLogin() {

        val email = binding.emailEt.text.toString().trim()
        val password = binding.passwordEt.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.login(email, password)
    }

    private fun observeState() {

        lifecycleScope.launch {

            viewModel.loginState.collect { state ->

                when (state) {

                    is Resource.Loading -> {
                        Toast.makeText(this@LoginAct, "Loading...", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Success -> {
                        startActivity(Intent(this@LoginAct, MainActivity::class.java))
                        finish()
                    }

                    is Resource.Error -> {
                        Toast.makeText(this@LoginAct, state.message, Toast.LENGTH_LONG).show()
                    }

                    null -> {}
                }
            }
        }
    }

}
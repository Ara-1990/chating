package com.the.chating.ui.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.the.chating.databinding.ActivityRegisterBinding
import android.Manifest
import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.the.chating.MainActivity
import com.the.chating.ui.users.UsersViewmodel
import kotlinx.coroutines.launch


class RegisterAct : AppCompatActivity() {


    private lateinit var binding: ActivityRegisterBinding

    private lateinit var viewModel: RegisterViewModel


    private var image_uri: Uri? = null

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                image_uri = it
                binding.profIv.setImageURI(it)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        binding.profIv.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.registerBtn.setOnClickListener {
            registerUser()
        }

        observeState()


    }


    private fun registerUser() {

        viewModel.register(
            binding.emailEt.text.toString(),
            binding.passwordEt.text.toString(),
            binding.nameEt.text.toString(),
            binding.descrProfEt.text.toString(),
            image_uri
        )
    }

    private fun observeState() {

        lifecycleScope.launch {
            viewModel.registerState.collect { state ->

                when (state) {

                    is Resource.Loading -> {
                        Toast.makeText(this@RegisterAct, "Loading...", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Success -> {
                        goToMain()
                    }

                    is Resource.Error -> {
                        Toast.makeText(this@RegisterAct, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


}
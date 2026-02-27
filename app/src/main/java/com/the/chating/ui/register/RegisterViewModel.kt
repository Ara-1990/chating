package com.the.chating.ui.register

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.the.chating.data.RegisterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel () : ViewModel() {

    private val repository = RegisterRepository()

    private val _registerState = MutableStateFlow<Resource<Unit>>(Resource.Loading)
    val registerState = _registerState.asStateFlow()

    fun register(
        email: String,
        password: String,
        name: String,
        description: String,
        imageUri: Uri?
    ) {

        viewModelScope.launch {

            _registerState.value = Resource.Loading

            val result = repository.registerUser(
                email,
                password,
                name,
                description,
                imageUri
            )

            _registerState.value = result
        }
    }
}
package com.the.chating.ui.login
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.the.chating.data.LoginRepository
import com.the.chating.ui.register.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repository = LoginRepository()

    private val _loginState = MutableStateFlow<Resource<Unit>?>(null)
    val loginState = _loginState.asStateFlow()

    fun login(email: String, password: String) {

        viewModelScope.launch {

            _loginState.value = Resource.Loading

            val result = repository.loginUser(email, password)

            _loginState.value = result
        }
    }
}
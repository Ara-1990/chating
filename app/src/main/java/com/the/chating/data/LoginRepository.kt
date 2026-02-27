package com.the.chating.data
import com.google.firebase.auth.FirebaseAuth
import com.the.chating.ui.register.Resource
import kotlinx.coroutines.tasks.await

class LoginRepository {
    private val auth = FirebaseAuth.getInstance()

    suspend fun loginUser(
        email: String,
        password: String
    ): Resource<Unit> {

        return try {

            auth.signInWithEmailAndPassword(email, password).await()

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }
}
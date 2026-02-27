package com.the.chating.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.the.chating.ui.register.Resource
import kotlinx.coroutines.tasks.await

class RegisterRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        description: String,
        imageUri: Uri?
    ): Resource<Unit> {

        return try {

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Resource.Error("User null")

            var imageUrl = ""

            if (imageUri != null) {
                val ref = storage.reference.child("ProfileImage/$uid")
                ref.putFile(imageUri).await()
                imageUrl = ref.downloadUrl.await().toString()
            }

            val map = hashMapOf<String, Any>(
                "uid" to uid,
                "email" to email,
                "name" to name,
                "description_profile" to description,
                "profileImage" to imageUrl,
                "timesTemp" to System.currentTimeMillis().toString()
            )

            database.reference.child("Users")
                .child(uid)
                .setValue(map).await()

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}
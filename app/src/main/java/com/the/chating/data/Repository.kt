package com.the.chating.data

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Repository {

    fun getUsersInfo(messagesList: MutableLiveData<List<UserModel>>) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val userList = mutableListOf<UserModel>()

        val usersRef = FirebaseDatabase.getInstance().getReference("Users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                val totalUsers = snapshot.children.count { it.key != currentUser.uid }
                var loadedUsers = 0


                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    if (userId == currentUser.uid) continue

                    val name = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    val profileImage =
                        userSnapshot.child("profileImage").getValue(String::class.java) ?: ""

                    val messagesRef = FirebaseDatabase.getInstance().getReference("Messages")
                    val chatId = if (currentUser.uid < userId) {
                        currentUser.uid + userId
                    } else {
                        userId + currentUser.uid
                    }


                    messagesRef.child(chatId).orderByChild("timestamp").limitToLast(1)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(messageSnapshot: DataSnapshot) {

                                var lastMessage = ""
                                var lastMessageType: String? = null

                                if (messageSnapshot.exists()) {

                                    val lastMsgSnapshot = messageSnapshot.children.firstOrNull()

                                    val message = lastMsgSnapshot
                                        ?.child("message")
                                        ?.getValue(String::class.java)

                                    val fileUrl = lastMsgSnapshot
                                        ?.child("fileUrl")
                                        ?.getValue(String::class.java)


                                    when {
                                        !message.isNullOrBlank() -> {
                                            lastMessage = message
                                            lastMessageType = "text"
                                        }

                                        !fileUrl.isNullOrBlank() -> {
                                            lastMessage = ""
                                            lastMessageType = "file"
                                        }
                                    }
                                }


                                val user = UserModel(
                                    uid = userId,
                                    name = name,
                                    profileImage = profileImage,
                                    message = lastMessage,
                                    lastMessageType = lastMessageType

                                )

                                userList.add(user)

                                loadedUsers++


                                if (loadedUsers == totalUsers) {
                                    messagesList.postValue(userList)
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                if (totalUsers == 0) {
                    messagesList.postValue(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }


    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun deleteChatWithUser(
        otherUserId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUserId = auth.uid ?: return
        val chatPath =
            if (currentUserId < otherUserId)
                currentUserId + otherUserId
            else
                otherUserId + currentUserId

        database.getReference("Messages")
            .child(chatPath)
            .removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Unknown error") }
    }


}
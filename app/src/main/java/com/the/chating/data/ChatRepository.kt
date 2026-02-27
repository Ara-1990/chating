package com.the.chating.data
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.the.chating.ui.register.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ChatRepository {

    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun listenMessages(chatPath: String) = callbackFlow<Resource<List<ChatMessage>>> {

        val ref = database.reference.child("Messages").child(chatPath)

        val listener = ref.addValueEventListener(object :
            com.google.firebase.database.ValueEventListener {

            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {

                val list = mutableListOf<ChatMessage>()

                for (child in snapshot.children) {
                    val msg = child.getValue(ChatMessage::class.java)
                    if (msg != null) list.add(msg)
                }

                list.sortBy { it.timestamp }

                trySend(Resource.Success(list))
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        })

        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun sendTextMessage(
        receiverId: String,
        messageText: String
    ): Resource<Unit> {

        return try {

            val senderId = auth.uid ?: return Resource.Error("User null")

            val chatPath =
                if (senderId < receiverId) senderId + receiverId
                else receiverId + senderId

            val timestamp = System.currentTimeMillis()
            val dateTime =
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(timestamp))

            val messageId = database.reference
                .child("Messages")
                .child(chatPath)
                .push().key!!

            val message = ChatMessage(
                messageId,
                senderId,
                receiverId,
                messageText,
                dateTime,
                timestamp,
                null,
                null,100
            )

            database.reference
                .child("Messages")
                .child(chatPath)
                .child(messageId)
                .setValue(message)
                .await()

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error sending message")
        }
    }

    suspend fun sendFile(receiverId: String, fileUri: Uri): Resource<Unit> {
        return try {

            val senderId = auth.uid ?: return Resource.Error("User null")

            val chatPath =
                if (senderId < receiverId) senderId + receiverId
                else receiverId + senderId

            val timestamp = System.currentTimeMillis()
            val dateTime =
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(timestamp))

            val messageId = database.reference
                .child("Messages")
                .child(chatPath)
                .push().key!!

            val fileType = storage.app.applicationContext.contentResolver.getType(fileUri)

            val storageRef = storage.reference
                .child("ChatFiles/$chatPath/$messageId")

            val message = ChatMessage(
                messageId,
                senderId,
                receiverId,
                "",
                dateTime,
                timestamp,
                "",
                fileType,
                0
            )

            database.reference
                .child("Messages")
                .child(chatPath)
                .child(messageId)
                .setValue(message)

            val uploadTask = storageRef.putFile(fileUri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress =
                    (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()

                database.reference
                    .child("Messages")
                    .child(chatPath)
                    .child(messageId)
                    .child("uploadingProgress")
                    .setValue(progress)
            }

            uploadTask.await()

            val downloadUrl = storageRef.downloadUrl.await()

            database.reference
                .child("Messages")
                .child(chatPath)
                .child(messageId)
                .child("fileUrl")
                .setValue(downloadUrl.toString())

            database.reference
                .child("Messages")
                .child(chatPath)
                .child(messageId)
                .child("uploadingProgress")
                .setValue(100)

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "File send error")
        }
    }
}
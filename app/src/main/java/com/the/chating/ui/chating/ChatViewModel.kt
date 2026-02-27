package com.the.chating.ui.chating
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.the.chating.data.ChatMessage
import com.the.chating.data.ChatRepository
import com.the.chating.ui.register.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _messagesState =
        MutableStateFlow<Resource<List<ChatMessage>>>(Resource.Loading)
    val messagesState = _messagesState.asStateFlow()

    fun listenMessages(chatPath: String) {
        viewModelScope.launch {
            repository.listenMessages(chatPath).collect {
                _messagesState.value = it
            }
        }
    }

    fun sendMessage(receiverId: String, text: String) {
        viewModelScope.launch {
            repository.sendTextMessage(receiverId, text)
        }
    }

    fun sendFile(receiverId: String, uri: Uri) {
        viewModelScope.launch {
            repository.sendFile(receiverId, uri)
        }
    }
}

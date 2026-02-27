package com.the.chating.ui.users
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.the.chating.data.UserModel
import com.the.chating.data.Repository

class UsersViewmodel : ViewModel() {
    private var repository = Repository()

    private val _messageResults = MutableLiveData<List<UserModel>>()
    val messageResults: LiveData<List<UserModel>> = _messageResults

    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> = _deleteResult


    fun loadUsers() {
        repository.getUsersInfo(_messageResults)
    }

    fun deleteChat(otherUserId: String) {
        repository.deleteChatWithUser(
            otherUserId,
            onSuccess = { _deleteResult.postValue(true) },
            onError = { _deleteResult.postValue(false) }
        )
    }
}
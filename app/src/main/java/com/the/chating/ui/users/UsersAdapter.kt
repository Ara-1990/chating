package com.the.chating.ui.users
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.the.chating.R
import com.the.chating.data.UserModel
import com.the.chating.databinding.MessageItemBinding
import com.the.chating.Constants
import androidx.navigation.findNavController

class UsersAdapter (
    private val onItemClick: (UserModel) -> Unit,
    private val deleteMessages: (UserModel, Int) -> Unit,
) : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    var publicationList = mutableListOf<UserModel>()

    class ViewHolder(val binding: MessageItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun updatePostList(publicationList: List<UserModel>) {
        this.publicationList.clear()
        this.publicationList.addAll(publicationList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user = publicationList[position]
        holder.binding.userName.text = user.name

        val messageText = when (user.lastMessageType) {
            "text" -> user.message ?: ""
            "image", "video", "file" -> "messages with attachments"
            else -> ""
        }

        holder.binding.userDescription.text = messageText


        Glide.with(holder.itemView.context)
            .load(user.profileImage)
            .into(holder.binding.userImage)

        holder.itemView.setOnClickListener {
            onItemClick(user)

        }

        holder.binding.btnMoreVert.setOnClickListener {
            deleteMessages(user, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = publicationList.size


}
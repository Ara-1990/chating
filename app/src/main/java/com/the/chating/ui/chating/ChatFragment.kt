package com.the.chating.ui.chating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.the.chating.R
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import com.google.firebase.database.FirebaseDatabase

import com.the.chating.databinding.FragmentChatBinding
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import java.util.Date
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.the.chating.Constants
import com.the.chating.data.ChatMessage

import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

import com.the.chating.data.UserModel
import com.the.chating.ui.register.Resource
import kotlinx.coroutines.launch


class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ChatViewModel
    private var receiverUser: UserModel? = null
    private lateinit var chatAdapter: ChatAdapter
    private var currentUserId: String? = null
    private var chatPath: String? = null
    private val selectedFileUris = mutableListOf<Uri>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val root: View = binding.root


        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        currentUserId = FirebaseAuth.getInstance().uid
        receiverUser = arguments?.getParcelable(Constants.KEY_USER)

        if (currentUserId == null || receiverUser == null) {
            Toast.makeText(requireContext(), "User data missing", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return binding.root
        }

        chatPath = getChatPath(currentUserId!!, receiverUser!!.uid!!)
        setupUI()
        setupRecyclerView()
        observeMessages()

        viewModel.listenMessages(chatPath!!)

        return root

    }

    private fun setupUI() {

        binding.txtName.text = receiverUser?.name ?: ""

        if (!receiverUser?.profileImage.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(receiverUser?.profileImage)
                .placeholder(R.drawable.ic_account_circle_24)
                .into(binding.imageProfile)
        }

        binding.buttonSend.setOnClickListener {

            val messageText = binding.inputMessage.text.toString().trim()

            if (selectedFileUris.isNotEmpty()) {


                if (messageText.isNotEmpty()) {
                    viewModel.sendMessage(receiverUser!!.uid!!, messageText)
                }


                selectedFileUris.forEach { uri ->
                    viewModel.sendFile(receiverUser!!.uid!!, uri)
                }

                selectedFileUris.clear()
                updateSendBadge()
                binding.inputMessage.text.clear()

                return@setOnClickListener
            }

            if (messageText.isNotEmpty()) {
                viewModel.sendMessage(receiverUser!!.uid!!, messageText)
                binding.inputMessage.text.clear()
            } else {
                Toast.makeText(requireContext(), "Enter message", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonAttachFile.setOnClickListener {
            pickFileLauncher.launch("*/*")
        }
    }

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedFileUris.add(it)
                updateSendBadge()
            }
        }

    private fun updateSendBadge() {

        if (selectedFileUris.isNotEmpty()) {
            binding.badgeTextView.text = selectedFileUris.size.toString()
            binding.badgeTextView.visibility = View.VISIBLE
        } else {
            binding.badgeTextView.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {

        chatAdapter = ChatAdapter(
            requireContext(),
            mutableListOf(),
            currentUserId!!,
            receiverUser?.profileImage
        )

        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = chatAdapter
    }

    private fun observeMessages() {

        lifecycleScope.launch {

            viewModel.messagesState.collect { state ->

                when (state) {

                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {

                        binding.progressBar.visibility = View.GONE

                        chatAdapter.updateMessages(state.data)

                        if (state.data.isNotEmpty()) {
                            binding.chatRecyclerView.scrollToPosition(state.data.size - 1)
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            state.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }


    private fun getChatPath(senderId: String, receiverId: String): String {
        return if (senderId < receiverId) senderId + receiverId else receiverId + senderId
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
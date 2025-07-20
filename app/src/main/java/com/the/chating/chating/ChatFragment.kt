package com.the.chating.chating

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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import java.util.Date
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

import com.the.chating.UserModel




class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private var receiverUser: UserModel? = null
    private var chatMessages = ArrayList<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter
    private var currentUserId: String? = null
    private lateinit var imageUri: Uri



    companion object {
        private const val REQUEST_CODE_GALLERY = 2001
        private const val REQUEST_CODE_CAMERA = 2002
        private const val REQUEST_CODE_DOCUMENT = 2003
        private const val REQUEST_CODE_AUDIO = 2004

        private val GALLERY_MIME_TYPES = arrayOf("image/*", "video/*")

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val root: View = binding.root

        currentUserId = FirebaseAuth.getInstance().uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show()
            return root
        }

        receiverUser = arguments?.getParcelable(Constants.KEY_USER)
        val profileImageUrl = receiverUser?.profileImage
        val name = receiverUser?.name

        if (!profileImageUrl.isNullOrEmpty()) {
            Glide.with(requireContext()).load(profileImageUrl)
                .placeholder(R.drawable.ic_account_circle).into(binding.imageProfile)
        }

        if (!name.isNullOrEmpty()) {
            binding.txtName.text = receiverUser!!.name
        }

        listenMessages()
        setListeners()
        return root

    }

    private fun sendMessage(messageText: String) {
        val senderId = FirebaseAuth.getInstance().uid
        val receiverId = receiverUser?.uid
        if (senderId == null || receiverId == null || messageText.isEmpty()) {
            Toast.makeText(requireContext(), "choose file or write message", Toast.LENGTH_LONG)
                .show()
            return
        }
        val timestamp = System.currentTimeMillis()
        val dateTime =
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))

        val chatPath = getChatPath(senderId, receiverId)
        val database = FirebaseDatabase.getInstance().reference
        val messageId = database.child("Messages").child(chatPath).push().key!!

        val message = ChatMessage(
            messageId = messageId,
            senderId = senderId,
            receiverId = receiverId,
            message = messageText,
            dateTime = dateTime,
            timestamp = timestamp,
            fileUrl = null,
            fileType = null,

            )

        database.child("Messages").child(chatPath).child(messageId).setValue(message)

            .addOnSuccessListener {
                binding.inputMessage.setText("")
            }
            .addOnFailureListener { e ->
                Log.e("ChatFragment", "Failed to write sender message: ${e.message}")
            }
    }

    private fun getChatPath(senderId: String, receiverId: String): String {
        return if (senderId < receiverId) senderId + receiverId else receiverId + senderId
    }

    private fun listenMessages() {
        binding.progressBar.visibility = View.GONE
        val curentUserId = FirebaseAuth.getInstance().uid
        val receiverId = receiverUser?.uid

        val chatPath = getChatPath(curentUserId!!, receiverId!!)
        var database = FirebaseDatabase.getInstance().reference

        database.child("Messages").child(chatPath)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatMessages.clear()


                    for (dataSnap in snapshot.children) {
                        val chatMessage = dataSnap.getValue(ChatMessage::class.java)
                        if (chatMessage != null) {
                            chatMessages.add(chatMessage)
                        }

                    }
                    chatMessages.sortBy { it.timestamp }

                    chatAdapter = ChatAdapter(
                        requireContext(),
                        chatMessages,
                        currentUserId!!,
                        receiverUser?.profileImage
                    )

                    binding.chatRecyclerView.layoutManager = LinearLayoutManager(context)
                    binding.chatRecyclerView.adapter = chatAdapter
                    chatAdapter.notifyDataSetChanged()
                    if (chatMessages.isNotEmpty()) {

                        binding.chatRecyclerView.post {
                            binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)
                        }
                    }

                    binding.progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()

                }

            })

    }

    var selectedFileUris = mutableListOf<Uri>()

    private fun setListeners() {

        binding.buttonSend.setOnClickListener {
            val messageText = binding.inputMessage.text.toString().trim()

            if (selectedFileUris.isNotEmpty()) {
                sendSelectedFiles(messageText)

            } else {
                Toast.makeText(requireContext(), " attach file or write text", Toast.LENGTH_SHORT)
                    .show()

            }
            binding.inputMessage.text.clear()


        }

        binding.buttonAttachFile.setOnClickListener {
            showAttachmentOptions()
        }
    }

    private fun showAttachmentOptions() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_attachment, null)

        val btnGallery = view.findViewById<Button>(R.id.btnGallery)
        btnGallery.setOnClickListener {
            bottomSheetDialog.dismiss()
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, GALLERY_MIME_TYPES)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

            }
            startActivityForResult(
                Intent.createChooser(intent, "Select Image or Video"),
                REQUEST_CODE_GALLERY
            )
        }

        view.findViewById<Button>(R.id.btnCamera).setOnClickListener {
            bottomSheetDialog.dismiss()
            if (hasCameraPermission()) {
                openCamera()

            } else {
                requestCameraPermission()
            }
        }

        view.findViewById<Button>(R.id.btnDocument).setOnClickListener {
            bottomSheetDialog.dismiss()
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                putExtra(
                    Intent.EXTRA_MIME_TYPES, arrayOf(
                        "application/pdf", "application/msword", "text/plain"
                    )
                )
            }
            startActivityForResult(Intent.createChooser(intent, "Select Document"), 2003)
        }

        view.findViewById<Button>(R.id.btnAudio).setOnClickListener {
            bottomSheetDialog.dismiss()
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/*"
            }
            startActivityForResult(Intent.createChooser(intent, "Select Audio"), 2004)
        }


        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_CODE_GALLERY,
            REQUEST_CODE_DOCUMENT,
            REQUEST_CODE_AUDIO,
                -> {
                selectedFileUris.clear()
                data!!.clipData?.let {
                    for (i in 0 until it.itemCount) {
                        selectedFileUris.add(it.getItemAt(i).uri)
                    }
                } ?: data?.data?.let {
                    selectedFileUris.add(it)
                }
                showSelectedFiles()

            }

            REQUEST_CODE_CAMERA -> {

                selectedFileUris.clear()
                selectedFileUris.add(imageUri)
                Toast.makeText(requireContext(), "Camera photo taken!", Toast.LENGTH_SHORT).show()
                showSelectedFiles()
            }
        }
    }


    private fun sendSelectedFiles(messageText: String) {
        if (selectedFileUris.isEmpty()) {
            Toast.makeText(requireContext(), "No file selected", Toast.LENGTH_SHORT).show()
            return
        }

        if (messageText.isNotEmpty()) {
            sendMessage(messageText)
        }

        selectedFileUris.forEach { uri ->
            uploadFileToFirebase(uri)
        }

        selectedFileUris.clear()
        showSelectedFiles()

    }

    private fun showSelectedFiles() {
        updateSendButtonBadge()
    }

    private fun uploadFileToFirebase(fileUri: Uri) {
        val mimeType = getMimeType(fileUri)

        val senderId = FirebaseAuth.getInstance().uid ?: return
        val receiverId = receiverUser?.uid ?: return
        val timestamp = System.currentTimeMillis()
        val dateTime =
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))

        val chatPath = getChatPath(senderId, receiverId)
        val database = FirebaseDatabase.getInstance().reference
        val messageId = database.child("Messages").child(chatPath).push().key!!

        val storageRef = FirebaseStorage.getInstance().reference
            .child("ChatFiles/$chatPath/$messageId")

        val message = ChatMessage(
            messageId = messageId,
            senderId = senderId,
            receiverId = receiverId,
            message = "",
            timestamp = timestamp,
            dateTime = dateTime,
            fileUrl = "",
            fileType = mimeType,
            uploadingProgress = 0,
            )

        database.child("Messages").child(chatPath).child(messageId).setValue(message)

        val uploadTask = storageRef.putFile(fileUri)


        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress =
                (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            database.child("Messages")
                .child(chatPath)
                .child(messageId)
                .child("uploadingProgress")
                .setValue(progress)
        }

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                message.fileUrl = uri.toString()
                message.uploadingProgress = 100

                database.child("Messages")
                    .child(chatPath)
                    .child(messageId)
                    .setValue(message)
            }.addOnFailureListener {}
        }.addOnFailureListener {

            Toast.makeText(requireContext(), "Upload failed: ${it.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun getMimeType(uri: Uri): String {
        val contentResolver = requireContext().contentResolver
        return contentResolver.getType(uri) ?: "application/octet-stream"
    }


    private fun updateSendButtonBadge() {
        val badgeTextView = binding.badgeTextView
        if (selectedFileUris.isNotEmpty()) {
            badgeTextView.text = selectedFileUris.size.toString()
            badgeTextView.visibility = View.VISIBLE
        } else {
            badgeTextView.visibility = View.GONE
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            101
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        val photoFile = File(requireContext().cacheDir, "camera_photo.jpg")
        imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivityForResult(intent, REQUEST_CODE_CAMERA)
    }


}
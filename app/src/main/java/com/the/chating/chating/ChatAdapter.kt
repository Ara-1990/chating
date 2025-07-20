package com.the.chating.chating

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.the.chating.PdfPreviewHelper
import com.the.chating.R
import com.the.chating.databinding.ItemConstantReceiverMessageBinding
import com.the.chating.databinding.ItemConstantSendMessageBinding

class ChatAdapter (
    private val context: Context,
    private val chatMessages: MutableList<ChatMessage>,
    private val currentUserId: String,
    private val receiverProfileImageUrl: String? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        const val VIEW_TYPE_SENT: Int = 1
        const val VIEW_TYPE_RECEIVED: Int = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            SentMessageViewHolder(
                ItemConstantSendMessageBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else {
            ReceivedMessageViewHolder(
                ItemConstantReceiverMessageBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMessage = chatMessages[position]

        when (holder.itemViewType) {
            VIEW_TYPE_SENT -> (holder as SentMessageViewHolder).bind(chatMessage)
            VIEW_TYPE_RECEIVED -> (holder as ReceivedMessageViewHolder).bind(chatMessage)
        }


    }


    override fun getItemViewType(position: Int): Int {
        return if (chatMessages.get(position).senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    inner class SentMessageViewHolder internal constructor(private val binding: ItemConstantSendMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.Q)
        @SuppressLint("SuspiciousIndentation")
        fun bind(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime ?: ""

            binding.fileContainer.visibility = View.GONE
            binding.textMessage.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
            binding.progressText.visibility = View.GONE
            binding.arrowIcon.visibility = View.GONE
            binding.videoLayout.root.visibility = View.GONE
            binding.pdfLayout.root.visibility = View.GONE
            binding.fileImageView.visibility = View.GONE

            if (!chatMessage.fileType.isNullOrEmpty()) {
                binding.fileContainer.visibility = View.VISIBLE
                binding.textMessage.visibility = View.GONE

                if (chatMessage.uploadingProgress in 0..99) {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressText.visibility = View.VISIBLE
                    binding.progressBar.progress = chatMessage.uploadingProgress
                    binding.progressText.text = "${chatMessage.uploadingProgress}%"

                    binding.fileImageView.visibility = View.GONE
                    binding.videoLayout.videoThumbnail.setImageResource(R.drawable.ic_video)
                    binding.pdfLayout.pdfThumbnail.setImageResource(R.drawable.ic_pdf_icon)

                } else if (chatMessage.uploadingProgress == 100 && !chatMessage.fileUrl.isNullOrEmpty()) {
                    binding.progressBar.visibility = View.GONE
                    binding.arrowIcon.visibility = View.VISIBLE
                    binding.progressText.visibility = View.GONE



                    when {
                        chatMessage.fileType?.startsWith("image") == true -> {

                            binding.fileImageView.visibility = View.VISIBLE
                            binding.videoLayout.root.visibility = View.GONE
                            binding.pdfLayout.root.visibility = View.GONE
                            Glide.with(binding.fileImageView.context)
                                .load(chatMessage.fileUrl)
                                .into(binding.fileImageView)


                        }

                        chatMessage.fileType?.startsWith("video") == true -> {

                            binding.videoLayout.root.visibility = View.VISIBLE
                            binding.pdfLayout.root.visibility = View.GONE
                            binding.fileImageView.visibility = View.GONE
                            Glide.with(context)
                                .load(chatMessage.fileUrl)
                                .thumbnail(0.1f)
                                .into(binding.videoLayout.videoThumbnail)

                        }

                        chatMessage.fileType?.startsWith("application/pdf") == true -> {

                            binding.videoLayout.root.visibility = View.GONE
                            binding.pdfLayout.root.visibility = View.VISIBLE
                            binding.fileImageView.visibility = View.GONE
                            PdfPreviewHelper.renderFromFirebaseUrl(
                                context,
                                chatMessage.fileUrl ?: "",
                                binding.pdfLayout.pdfThumbnail
                            )


                        }

                        else -> {

                            binding.pdfLayout.root.visibility = View.GONE
                            binding.videoLayout.root.visibility = View.GONE
                            binding.fileImageView.setImageResource(R.drawable.ic_file_placeholder)
                            binding.fileImageView.visibility = View.VISIBLE
                        }
                    }


                }

            }

            binding.fileContainer.setOnClickListener {
                openFile(chatMessage.fileUrl)
            }
        }
    }


    inner class ReceivedMessageViewHolder internal constructor(private val binding: ItemConstantReceiverMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.Q)
        fun bind(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime ?: ""


            if (!receiverProfileImageUrl.isNullOrEmpty()) {
                Glide.with(binding.imageProfile.context)
                    .load(receiverProfileImageUrl)
                    .placeholder(R.drawable.ic_account_circle)
                    .into(binding.imageProfile)
            } else {
                binding.imageProfile.setImageResource(R.drawable.ic_account_circle)
            }


            binding.fileContainer.visibility = View.GONE
            binding.textMessage.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
            binding.progressText.visibility = View.GONE
            binding.downloadArrow.visibility = View.GONE
            binding.videoLayout.root.visibility = View.GONE
            binding.pdfLayout.root.visibility = View.GONE
            binding.fileImageView.visibility = View.GONE
            binding.imageProfile.visibility = View.GONE

            if (!chatMessage.fileType.isNullOrEmpty()) {

                binding.fileContainer.visibility = View.VISIBLE
                binding.textMessage.visibility = View.GONE

                if (chatMessage.uploadingProgress in 0..99) {

                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressText.visibility = View.VISIBLE
                    binding.progressBar.progress = chatMessage.uploadingProgress
                    binding.progressText.text = "${chatMessage.uploadingProgress}%"


                    binding.videoLayout.videoThumbnail.setImageResource(R.drawable.ic_video)
                    binding.pdfLayout.pdfThumbnail.setImageResource(R.drawable.ic_pdf_icon)

                } else if (chatMessage.uploadingProgress == 100 && !chatMessage.fileUrl.isNullOrEmpty()) {

                    binding.progressBar.visibility = View.GONE
                    binding.progressText.visibility = View.GONE
                    binding.downloadArrow.visibility = View.VISIBLE
                    binding.imageProfile.visibility = View.VISIBLE

                    when {
                        chatMessage.fileType!!.startsWith("image") -> {
                            binding.fileImageView.visibility = View.VISIBLE
                            binding.videoLayout.root.visibility = View.GONE
                            binding.pdfLayout.root.visibility = View.GONE

                            Glide.with(binding.fileImageView.context)
                                .load(chatMessage.fileUrl)
                                .into(binding.fileImageView)
                        }

                        chatMessage.fileType!!.startsWith("video") -> {
                            binding.videoLayout.root.visibility = View.VISIBLE
                            binding.pdfLayout.root.visibility = View.GONE
                            binding.fileImageView.visibility = View.GONE

                            Glide.with(context)
                                .load(chatMessage.fileUrl)
                                .thumbnail(0.1f)
                                .into(binding.videoLayout.videoThumbnail)
                        }

                        chatMessage.fileType!!.startsWith("application/pdf") -> {
                            binding.pdfLayout.root.visibility = View.VISIBLE
                            binding.videoLayout.root.visibility = View.GONE
                            binding.fileImageView.visibility = View.GONE

                            PdfPreviewHelper.renderFromFirebaseUrl(
                                context,
                                chatMessage.fileUrl ?: "",
                                binding.pdfLayout.pdfThumbnail
                            )
                        }

                        else -> {

                            binding.pdfLayout.root.visibility = View.GONE
                            binding.videoLayout.root.visibility = View.GONE
                            binding.fileImageView.setImageResource(R.drawable.ic_file_placeholder)
                            binding.fileImageView.visibility = View.VISIBLE
                        }
                    }
                }
            }


            binding.fileContainer.setOnClickListener {
                openFile(chatMessage.fileUrl)
            }
        }

    }




    private fun openFile(fileUrl: String?) {
        fileUrl?.let {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(it), "*/*")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}
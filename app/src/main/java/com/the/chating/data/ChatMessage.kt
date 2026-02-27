package com.the.chating.data

data class ChatMessage(
    var messageId: String = "",
    var senderId: String? = null,
    var receiverId: String? = null,
    var message: String? = null,
    var dateTime: String? = null,
    var timestamp: Long? = null,
    var fileUrl: String? = null,
    var fileType: String? = null,
    var uploadingProgress: Int = 0,

)
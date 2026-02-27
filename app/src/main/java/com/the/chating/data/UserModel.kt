package com.the.chating.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class UserModel (
    var uid:String? = null,
    var name:String? = null,
    var description_profile: String? = null,
    var profileImage:String? = null,
    var message:String? = null,
    var lastMessageType: String? = null
    ): Parcelable
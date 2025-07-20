package com.the.chating

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.versionedparcelable.ParcelField
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class UserModel (
    var uid:String? = null,
    var name:String? = null,
    var description_profile: String? = null,
    var profileImage:String? = null,
    ): Parcelable
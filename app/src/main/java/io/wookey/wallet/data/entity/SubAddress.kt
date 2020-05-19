package io.wookey.wallet.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubAddress @JvmOverloads constructor(var id: Int = 0, var address: String, var label: String?) : Parcelable
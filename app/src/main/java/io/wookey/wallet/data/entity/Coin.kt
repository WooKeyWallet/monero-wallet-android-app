package io.wookey.wallet.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Coin(
        var symbol: String = "",
        var coin: String = "",
        var imageUrl: String = "") : Parcelable
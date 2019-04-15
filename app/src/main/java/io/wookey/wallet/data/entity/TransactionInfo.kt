package io.wookey.wallet.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TransactionInfo(
        var token: String = "",
        var assetId: Int = 0,
        var direction: Int = 0,// 0 接收 1 发送
        var isPending: Boolean = false,
        var isFailed: Boolean = false,
        var amount: String? = "",
        var fee: String? = "",
        var blockHeight: Long = 0,
        var confirmations: Long = 0,
        var hash: String? = "",
        var timestamp: Long = 0,
        var paymentId: String? = "",
        var txKey: String? = "",
        var address: String? = ""
) : Parcelable
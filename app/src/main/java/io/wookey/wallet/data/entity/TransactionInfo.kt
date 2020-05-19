package io.wookey.wallet.data.entity

import android.arch.persistence.room.*
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "transactionInfo")
data class TransactionInfo(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Int = 0,
        @ColumnInfo
        var token: String = "",
        @ColumnInfo
        var assetId: Int = 0,
        @ColumnInfo
        var walletId: Int = 0,
        @ColumnInfo
        var direction: Int = 0,// 0 接收 1 发送
        @ColumnInfo
        var isPending: Boolean = false,
        @ColumnInfo
        var isFailed: Boolean = false,
        @ColumnInfo
        var amount: String? = "",
        @ColumnInfo
        var fee: String? = "",
        @ColumnInfo
        var blockHeight: Long = 0,
        @ColumnInfo
        var confirmations: Long = 0,
        @ColumnInfo
        var hash: String? = "",
        @ColumnInfo
        var timestamp: Long = 0,
        @ColumnInfo
        var paymentId: String? = "",
        @ColumnInfo
        var txKey: String? = "",
        @ColumnInfo
        var address: String? = "",
        @ColumnInfo
        var subAddressLabel: String? = ""
) : Parcelable
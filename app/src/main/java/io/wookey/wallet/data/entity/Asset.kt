package io.wookey.wallet.data.entity

import android.arch.persistence.room.*
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "assets", indices = [Index(value = arrayOf("walletId", "contractAddress"), unique = true)])
data class Asset @JvmOverloads constructor(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Int = 0,
        @ColumnInfo
        var walletId: Int = 0,
        @ColumnInfo
        var token: String = "",
        @ColumnInfo
        var balance: String = "",
        @ColumnInfo
        var contractAddress: String = "") : Parcelable
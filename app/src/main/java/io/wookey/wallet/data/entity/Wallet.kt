package io.wookey.wallet.data.entity

import android.arch.persistence.room.*
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "wallets", indices = [Index(value = arrayOf("symbol", "name"), unique = true)])
data class Wallet @JvmOverloads constructor(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Int = 0,
        @ColumnInfo
        var symbol: String = "",
        @ColumnInfo
        var name: String = "",
        @ColumnInfo
        var address: String = "",
        @ColumnInfo
        var balance: String = "",
        @ColumnInfo
        var passwordPrompt: String = "",
        @ColumnInfo
        var restoreHeight: Long = 0,
        @ColumnInfo
        var isActive: Boolean = false,
        @Ignore
        var seed: String = "") : Parcelable
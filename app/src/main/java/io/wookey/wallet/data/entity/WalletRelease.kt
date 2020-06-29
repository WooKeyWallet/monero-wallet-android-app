package io.wookey.wallet.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "walletRelease", indices = [Index(value = arrayOf("walletId"), unique = true)])
data class WalletRelease @JvmOverloads constructor(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Int = 0,
        @ColumnInfo
        var walletId: Int = 0,
        @ColumnInfo
        var password: String = "",
        @ColumnInfo
        var iv: String = "",
        @ColumnInfo
        var openWallet: Boolean = false,
        @ColumnInfo
        var sendTransaction: Boolean = false,
        @ColumnInfo
        var backup: Boolean = false,
        @ColumnInfo
        var fingerprint: Boolean = false,
        @ColumnInfo
        var pattern: Boolean = false,
        @ColumnInfo
        var patternPassword: String? = ""
) : Parcelable
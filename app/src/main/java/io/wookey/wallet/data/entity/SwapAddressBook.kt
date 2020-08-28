package io.wookey.wallet.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "swap_address_books", indices = [Index(value = arrayOf("symbol", "address", "notes"), unique = true)])
data class SwapAddressBook @JvmOverloads constructor(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Int = 0,
        @ColumnInfo
        var symbol: String = "",
        @ColumnInfo
        var address: String = "",
        @ColumnInfo
        var notes: String = "") : Parcelable
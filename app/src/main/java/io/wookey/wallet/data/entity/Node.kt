package io.wookey.wallet.data.entity

import android.arch.persistence.room.*
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "nodes", indices = [Index(value = arrayOf("symbol", "url"), unique = true)])
data class Node @JvmOverloads constructor(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Int = 0,
        @ColumnInfo
        var symbol: String = "",
        @ColumnInfo
        var url: String = "",
        @Ignore
        @ColumnInfo
        var responseTime: Long = -1,
        @ColumnInfo
        var isSelected: Boolean = false) : Parcelable
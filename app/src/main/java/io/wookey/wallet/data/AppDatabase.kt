package io.wookey.wallet.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import io.wookey.wallet.App
import io.wookey.wallet.data.dao.AddressBookDao
import io.wookey.wallet.data.dao.AssetDao
import io.wookey.wallet.data.dao.NodeDao
import io.wookey.wallet.data.dao.WalletDao
import io.wookey.wallet.data.entity.AddressBook
import io.wookey.wallet.data.entity.Asset
import io.wookey.wallet.data.entity.Node
import io.wookey.wallet.data.entity.Wallet

@Database(entities = [Wallet::class, Asset::class, Node::class, AddressBook::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun assetDao(): AssetDao
    abstract fun nodeDao(): NodeDao
    abstract fun addressBookDao(): AddressBookDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context = App.instance): AppDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "Wallet.db")
                        .build()

    }
}
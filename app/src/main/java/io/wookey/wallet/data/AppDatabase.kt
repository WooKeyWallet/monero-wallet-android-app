package io.wookey.wallet.data

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import android.content.Context
import io.wookey.wallet.App
import io.wookey.wallet.data.dao.*
import io.wookey.wallet.data.entity.*

@Database(entities = [Wallet::class, Asset::class, Node::class, AddressBook::class, TransactionInfo::class], version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun assetDao(): AssetDao
    abstract fun nodeDao(): NodeDao
    abstract fun addressBookDao(): AddressBookDao
    abstract fun transactionInfoDao(): TransactionInfoDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `transactionInfo` " +
                        "(`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`token` TEXT NOT NULL, " +
                        "`assetId` INTEGER NOT NULL, " +
                        "`walletId` INTEGER NOT NULL, " +
                        "`direction` INTEGER NOT NULL, " +
                        "`isPending` INTEGER NOT NULL, " +
                        "`isFailed` INTEGER NOT NULL, " +
                        "`amount` TEXT, " +
                        "`fee` TEXT, " +
                        "`blockHeight` INTEGER NOT NULL, " +
                        "`confirmations` INTEGER NOT NULL, " +
                        "`hash` TEXT, " +
                        "`timestamp` INTEGER NOT NULL," +
                        " `paymentId` TEXT," +
                        " `txKey` TEXT, " +
                        "`address` TEXT)")

            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `transactionInfo` "
                            + " ADD COLUMN `subAddressLabel` TEXT")
            }
        }

        fun getInstance(context: Context = App.instance): AppDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "Wallet.db")
                        .addMigrations(MIGRATION_1_2)
                        .addMigrations(MIGRATION_2_3)
                        .build()

    }
}
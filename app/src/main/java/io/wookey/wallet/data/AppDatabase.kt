package io.wookey.wallet.data

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import android.content.Context
import io.wookey.wallet.App
import io.wookey.wallet.data.dao.*
import io.wookey.wallet.data.entity.*

@Database(
    entities = [Wallet::class, Asset::class, Node::class, AddressBook::class, TransactionInfo::class,
        WalletRelease::class, SwapAddressBook::class, SwapRecord::class],
    version = 5
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun assetDao(): AssetDao
    abstract fun nodeDao(): NodeDao
    abstract fun addressBookDao(): AddressBookDao
    abstract fun transactionInfoDao(): TransactionInfoDao
    abstract fun walletReleaseDao(): WalletReleaseDao
    abstract fun swapAddressBookDao(): SwapAddressBookDao
    abstract fun swapRecordDao(): SwapRecordDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE `transactionInfo` " +
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
                            "`address` TEXT)"
                )

            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE `transactionInfo` "
                            + " ADD COLUMN `subAddressLabel` TEXT"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE `walletRelease` " +
                            "(`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`walletId` INTEGER NOT NULL, " +
                            "`password` TEXT NOT NULL, " +
                            "`iv` TEXT NOT NULL, " +
                            "`openWallet` INTEGER NOT NULL, " +
                            "`sendTransaction` INTEGER NOT NULL," +
                            "`backup` INTEGER NOT NULL," +
                            "`fingerprint` INTEGER NOT NULL," +
                            "`pattern` INTEGER NOT NULL," +
                            "`patternPassword` TEXT)"
                )
                database.execSQL("CREATE UNIQUE INDEX `index_walletRelease_walletId` ON `walletRelease` (`walletId`)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE `swap_address_books` " +
                            "(`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`symbol` TEXT NOT NULL, " +
                            "`address` TEXT NOT NULL, " +
                            "`notes` TEXT NOT NULL)"
                )
                database.execSQL("CREATE UNIQUE INDEX `index_swap_address_books_symbol_address_notes` ON `swap_address_books` (`symbol`, `address`, `notes`)")
                database.execSQL(
                    "CREATE TABLE `swapRecords` " +
                            "(`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`swapId` TEXT NOT NULL, " +
                            "`amountFrom` TEXT NOT NULL, " +
                            "`currencyFrom` TEXT NOT NULL, " +
                            "`amountTo` TEXT NOT NULL, " +
                            "`currencyTo` TEXT NOT NULL, " +
                            "`createdAt` TEXT NOT NULL)"
                )
                database.execSQL("CREATE UNIQUE INDEX `index_swapRecords_swapId` ON `swapRecords` (`swapId`)")
            }
        }

        fun getInstance(context: Context = App.instance): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "Wallet.db"
            )
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .build()

    }
}
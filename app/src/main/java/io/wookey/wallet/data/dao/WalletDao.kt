package io.wookey.wallet.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import io.wookey.wallet.data.entity.Wallet

@Dao
interface WalletDao {

    /**
     * Insert a wallet in the database. If the wallet already exists, ignore it.
     *
     * @param wallet the wallet to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertWallet(wallet: Wallet)

    /**
     * Select wallets from the wallets table by symbol.
     *
     * @return wallets.
     */
    @Query("SELECT * FROM wallets WHERE symbol = :symbol ORDER BY _id")
    fun loadSymbolWallets(symbol: String): LiveData<List<Wallet>>

    /**
     * Select all wallets count from the wallets table.
     *
     * @return wallets count.
     */
    @Query("SELECT COUNT(*) FROM wallets")
    fun countWallets(): Int

    /**
     * Select all wallets count from the wallets table.
     *
     * @return wallets count.
     */
    @Query("SELECT COUNT(*) FROM wallets WHERE symbol = :symbol AND name = :name")
    fun countWalletsByName(symbol: String, name: String): Int

    /**
     * Select wallets from the wallets table.
     *
     * @return wallets.
     */
    @Query("SELECT * FROM wallets")
    fun getWallets(): List<Wallet>

    /**
     * Select the active wallet from the wallets table.
     *
     * @return active wallet.
     */
    @Query("SELECT * FROM wallets WHERE isActive = 1")
    fun getActiveWallet(): Wallet?

    /**
     * Select the active wallets from the wallets table.
     *
     * @return active wallets.
     */
    @Query("SELECT * FROM wallets WHERE isActive = 1")
    fun getActiveWallets(): List<Wallet>?

    /**
     * Select wallet from the wallets table by symbol and name.
     *
     * @return wallet.
     */
    @Query("SELECT * FROM wallets WHERE symbol = :symbol AND name = :name")
    fun getWalletsByName(symbol: String, name: String): Wallet?

    /**
     * Select the wallet from the wallets table by id.
     *
     * @return wallet.
     */
    @Query("SELECT * FROM wallets WHERE _id = :id")
    fun getWalletById(id: Int): Wallet?

    /**
     * Select the wallet from the wallets table by id.
     *
     * @return wallet.
     */
    @Query("SELECT * FROM wallets WHERE _id = :id")
    fun loadWalletById(id: Int): LiveData<Wallet>

    /**
     * Select the active wallet from the wallets table.
     *
     * @return active wallet.
     */
    @Query("SELECT * FROM wallets WHERE isActive = 1")
    fun loadActiveWallet(): LiveData<Wallet>

    /**
     * Select symbol from the wallets table group by symbol.
     *
     * @return symbol group.
     */
    @Query("SELECT symbol FROM wallets GROUP BY symbol")
    fun loadWalletSymbol(): LiveData<List<String>>

    /**
     * Update wallets in the database
     *
     * @param wallets the wallets to be updated.
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateWallets(vararg wallets: Wallet)

    /**
     * Delete wallets in the database
     *
     * @param wallets the wallets to be deleted.
     */
    @Delete
    fun deleteWallets(vararg wallets: Wallet)
}
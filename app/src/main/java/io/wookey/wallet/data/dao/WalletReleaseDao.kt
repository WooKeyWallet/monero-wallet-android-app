package io.wookey.wallet.data.dao

import androidx.room.*
import io.wookey.wallet.data.entity.WalletRelease

@Dao
interface WalletReleaseDao {
    /**
     * Insert a walletRelease in the database. If the walletRelease already exists, replace it.
     *
     * @param walletRelease the walletRelease to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(walletRelease: WalletRelease)

    /**
     * Select all walletRelease from the walletRelease table by walletId.
     *
     * @return walletRelease.
     */
    @Query("SELECT * FROM walletRelease WHERE walletId = :walletId")
    fun loadDataByWalletId(walletId: Int): WalletRelease?

    /**
     * Update a walletRelease in the database
     *
     * @param walletRelease the walletRelease to be updated.
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(walletRelease: WalletRelease)

    /**
     * Delete a walletRelease in the database
     *
     * @param walletRelease the walletRelease to be deleted.
     */
    @Delete
    fun delete(walletRelease: WalletRelease)
}
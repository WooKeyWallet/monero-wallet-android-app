package io.wookey.wallet.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import io.wookey.wallet.data.entity.Asset

@Dao
interface AssetDao {

    /**
     * Insert a asset in the database. If the asset already exists, replace it.
     *
     * @param asset the asset to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAsset(asset: Asset)

    /**
     * Select all assets from the wallets table.
     *
     * @return all assets.
     */
    @Query("SELECT * FROM assets")
    fun loadAssets(): LiveData<List<Asset>>

    /**
     * Select all assets from the wallets table by walletId.
     *
     * @return assets.
     */
    @Query("SELECT * FROM assets WHERE walletId = :walletId")
    fun loadAssetsByWalletId(walletId: Int): LiveData<List<Asset>>

    /**
     * Select all assets from the wallets table by walletId.
     *
     * @return assets.
     */
    @Query("SELECT * FROM assets WHERE walletId = :walletId")
    fun getAssetsByWalletId(walletId: Int): List<Asset>

    /**
     * Select asset from the wallets table by id.
     *
     * @return asset.
     */
    @Query("SELECT * FROM assets WHERE _id = :id")
    fun getAssetById(id: Int): Asset?

    /**
     * Select asset from the wallets table by id.
     *
     * @return asset.
     */
    @Query("SELECT * FROM assets WHERE _id = :id")
    fun loadAssetById(id: Int): LiveData<Asset>

    /**
     * Update a asset in the database
     *
     * @param asset the asset to be updated.
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAsset(asset: Asset)

    /**
     * Delete assets in the database
     *
     * @param assets the assets to be deleted.
     */
    @Delete
    fun deleteAssets(vararg assets: Asset)
}
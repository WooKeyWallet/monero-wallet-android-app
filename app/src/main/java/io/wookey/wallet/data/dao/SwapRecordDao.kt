package io.wookey.wallet.data.dao

import androidx.room.*
import io.wookey.wallet.data.entity.SwapRecord
import io.wookey.wallet.data.entity.TransactionInfo
import io.wookey.wallet.data.entity.WalletRelease

@Dao
interface SwapRecordDao {

    /**
     * Insert a swapRecord in the database. If the swapRecord already exists, replace it.
     *
     * @param swapRecord the swapRecord to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(swapRecord: SwapRecord)

    /**
     * Select SwapRecord from the swapRecords table by swapId.
     *
     * @return SwapRecord.
     */
    @Query("SELECT * FROM swapRecords WHERE swapId = :swapId")
    fun getSwapRecordBySwapId(swapId: String): SwapRecord?

    /**
     * Select SwapRecord from the swapRecords table by swapId.
     *
     * @return SwapRecord.
     */
    @Query("SELECT * FROM swapRecords ORDER BY _id DESC LIMIT :limit OFFSET :offset")
    fun getSwapRecords(offset: Int, limit: Int = 20): List<SwapRecord>

    /**
     * Update a swapRecord in the database
     *
     * @param swapRecord the swapRecord to be updated.
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(swapRecord: SwapRecord)
}
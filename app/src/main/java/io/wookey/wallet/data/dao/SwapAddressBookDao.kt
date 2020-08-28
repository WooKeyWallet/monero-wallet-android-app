package io.wookey.wallet.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import io.wookey.wallet.data.entity.AddressBook
import io.wookey.wallet.data.entity.Asset
import io.wookey.wallet.data.entity.SwapAddressBook

@Dao
interface SwapAddressBookDao {

    /**
     * Insert a addressBook in the database.
     *
     * @param addressBook the task to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(addressBook: SwapAddressBook)

    /**
     * Select all addressBooks from the address_books table.
     *
     * @return all addressBooks.
     */
    @Query("SELECT * FROM swap_address_books ORDER BY _id DESC")
    fun loadAddressBooks(): LiveData<List<SwapAddressBook>>

    /**
     * Select all addressBooks from the address_books table by symbol.
     *
     * @return all addressBooks.
     */
    @Query("SELECT * FROM swap_address_books WHERE symbol=:symbol ORDER BY _id DESC")
    fun loadAddressBooksBySymbol(symbol: String): LiveData<List<SwapAddressBook>>

    /**
     * Select all addressBooks from the address_books table by symbol and address.
     *
     * @return all addressBooks.
     */
    @Query("SELECT * FROM swap_address_books WHERE symbol=:symbol AND address=:address ORDER BY _id DESC")
    fun loadAddressBooksBySymbolAndAddress(symbol: String, address: String): List<SwapAddressBook>?

    /**
     * Update a addressBook in the database
     *
     * @param addressBook the addressBook to be updated.
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(addressBook: SwapAddressBook)

    /**
     * Delete addressBook in the database
     *
     * @param addressBook the addressBook to be deleted.
     */
    @Delete
    fun delete(addressBook: SwapAddressBook)
}
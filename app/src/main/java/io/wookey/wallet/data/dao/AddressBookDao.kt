package io.wookey.wallet.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import io.wookey.wallet.data.entity.AddressBook
import io.wookey.wallet.data.entity.Asset

@Dao
interface AddressBookDao {

    /**
     * Insert a addressBook in the database.
     *
     * @param addressBook the task to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAddressBook(addressBook: AddressBook)

    /**
     * Select all addressBooks from the address_books table.
     *
     * @return all addressBooks.
     */
    @Query("SELECT * FROM address_books ORDER BY _id DESC")
    fun loadAddressBooks(): LiveData<List<AddressBook>>

    /**
     * Select all addressBooks from the address_books table by symbol.
     *
     * @return all addressBooks.
     */
    @Query("SELECT * FROM address_books WHERE symbol=:symbol ORDER BY _id DESC")
    fun loadAddressBooksBySymbol(symbol: String): LiveData<List<AddressBook>>

    /**
     * Update a addressBook in the database
     *
     * @param addressBook the addressBook to be updated.
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAddressBook(addressBook: AddressBook)

    /**
     * Delete addressBook in the database
     *
     * @param addressBook the addressBook to be deleted.
     */
    @Delete
    fun deleteAddressBook(addressBook: AddressBook)
}
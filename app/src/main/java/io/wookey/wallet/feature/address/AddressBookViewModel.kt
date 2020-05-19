package io.wookey.wallet.feature.address

import android.arch.lifecycle.MutableLiveData
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.AddressBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddressBookViewModel : BaseViewModel() {

    val isDelete = MutableLiveData<Boolean>()
    val itemClick = MutableLiveData<AddressBook>()
    val editAddressBook = MutableLiveData<AddressBook>()
    val deleteSuccess = MutableLiveData<Int>()

    fun deleteAddressBook(it: AddressBook, position: Int) {
        isDelete.value = true
        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    AppDatabase.getInstance().addressBookDao().deleteAddressBook(it)
                    deleteSuccess.postValue(position)
                } catch (e: Exception) {
                    e.printStackTrace()
                    deleteSuccess.postValue(-1)
                }
            }
        }
    }

    fun itemClick(addressBook: AddressBook) {
        itemClick.value = addressBook
    }

    fun edit(addressBook: AddressBook) {
        editAddressBook.value = addressBook
    }
}
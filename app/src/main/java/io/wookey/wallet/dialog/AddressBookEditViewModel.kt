package io.wookey.wallet.dialog

import android.arch.lifecycle.MutableLiveData
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.AddressBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddressBookEditViewModel : BaseViewModel() {

    val success = MutableLiveData<Boolean>()
    val showLoading = MutableLiveData<Boolean>()
    val hideLoading = MutableLiveData<Boolean>()
    val toastRes = MutableLiveData<Int>()

    fun updateAddressBook(addressBook: AddressBook) {
        showLoading.value = true
        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    AppDatabase.getInstance().addressBookDao().updateAddressBook(addressBook)
                    success.postValue(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    toastRes.postValue(R.string.data_exception)
                }
            }
            hideLoading.postValue(true)
        }
    }

}
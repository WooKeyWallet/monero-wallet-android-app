package io.wookey.wallet.feature.address

import android.app.Activity
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.core.XMRWalletController
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.AddressBook
import io.wookey.wallet.support.REQUEST_SCAN_ADDRESS
import io.wookey.wallet.support.REQUEST_SELECT_COIN
import io.wookey.wallet.support.viewmodel.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddAddressViewModel : BaseViewModel() {

    val notes = MutableLiveData<String>()
    val address = MutableLiveData<String>()
    val scanAddress = MutableLiveData<String>()
    val coin = MutableLiveData<String>()
    val enabled = MediatorLiveData<Boolean>()
    val navigation = SingleLiveEvent<Unit>()

    val addressError = MutableLiveData<Boolean>()

    init {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val wallet = AppDatabase.getInstance().walletDao().getActiveWallet()
                coin.postValue(wallet?.symbol)
            }
        }
        enabled.addSource(notes) {
            enabled.value = checkValid()
        }
        enabled.addSource(address) {
            enabled.value = checkValid()
        }
        enabled.addSource(coin) {
            enabled.value = checkValid()
        }
    }

    private fun checkValid(): Boolean {
        if (notes.value.isNullOrBlank()) {
            return false
        }
        if (address.value.isNullOrBlank()) {
            return false
        }
        val error1 = addressError.value
        if (error1 != null && error1) {
            return false
        }
        if (coin.value.isNullOrBlank()) {
            return false
        }
        return true
    }

    fun addressChanged(it: String) {
        if (it.isNullOrBlank()) {
            addressError.value = null
        } else {
            addressError.value = !XMRWalletController.isAddressValid(it)
        }
        address.value = it
    }

    fun next() {
        uiScope.launch {
            try {
                enabled.value = false
                withContext(Dispatchers.IO) {
                    AppDatabase.getInstance().addressBookDao().insertAddressBook(AddressBook().also {
                        it.symbol = coin.value!!
                        it.address = address.value!!
                        it.notes = notes.value!!
                    })
                }
                navigation.call()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                enabled.value = true
            }
        }
    }

    fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_SCAN_ADDRESS -> {
                data?.getStringExtra("result")?.let {
                    if (it.isNotBlank()) {
                        scanAddress.value = it
                    }
                }
            }
        }
    }

}
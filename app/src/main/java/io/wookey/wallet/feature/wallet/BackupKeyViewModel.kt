package io.wookey.wallet.feature.wallet

import android.arch.lifecycle.MutableLiveData
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.core.XMRRepository
import io.wookey.wallet.core.XMRWalletController
import io.wookey.wallet.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BackupKeyViewModel : BaseViewModel() {

    private val repository = XMRRepository()

    val publicViewKey = MutableLiveData<String>()
    val secretViewKey = MutableLiveData<String>()
    val publicSpendKey = MutableLiveData<String>()
    val secretSpendKey = MutableLiveData<String>()
    val address = MutableLiveData<String>()

    val showLoading = MutableLiveData<Boolean>()
    val hideLoading = MutableLiveData<Boolean>()
    val toast = MutableLiveData<String>()
    val toastRes = MutableLiveData<Int>()

    fun openWallet(walletId: Int, password: String) {
        showLoading.postValue(true)
        uiScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val wallet = AppDatabase.getInstance().walletDao().getWalletById(walletId)
                    if (wallet == null) {
                        toastRes.postValue(R.string.data_exception)
                    } else {
                        val path = repository.getWalletFilePath(wallet.name)
                        XMRWalletController.openWallet(path, password)
                        publicViewKey.postValue(XMRWalletController.getPublicViewKey())
                        secretViewKey.postValue(XMRWalletController.getSecretViewKey())
                        publicSpendKey.postValue(XMRWalletController.getPublicSpendKey())
                        secretSpendKey.postValue(XMRWalletController.getSecretSpendKey())
                        address.postValue(wallet.address)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                toast.postValue(e.message)
            } finally {
                hideLoading.postValue(true)
            }
        }
    }
}
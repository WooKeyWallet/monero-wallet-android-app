package io.wookey.wallet.dialog

import android.arch.lifecycle.MutableLiveData
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.core.XMRRepository
import io.wookey.wallet.core.XMRWalletController
import io.wookey.wallet.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PasswordViewModel : BaseViewModel() {

    private val xmrRepository = XMRRepository()

    val verifyPassed = MutableLiveData<String>()
    val verifyFailed = MutableLiveData<Boolean>()

    fun verify(password: String, walletId: Int) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val wallet = AppDatabase.getInstance().walletDao().getWalletById(walletId)
                            ?: throw IllegalStateException()
                    val name = wallet.name
                    val keyPath = xmrRepository.getKeysFilePath(name)
                    val verify = XMRWalletController.verifyWalletPasswordOnly(keyPath, password)
                    if (verify) {
                        verifyPassed.postValue(password)
                    } else {
                        verifyFailed.postValue(true)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    verifyFailed.postValue(true)
                }
            }
        }
    }
}
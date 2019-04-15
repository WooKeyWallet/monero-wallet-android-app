package io.wookey.wallet.feature.wallet

import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.Wallet
import io.wookey.wallet.support.viewmodel.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletDetailViewModel : BaseViewModel() {

    val wallet = MutableLiveData<Wallet>()
    val showPasswordPrompt = MutableLiveData<String>()
    val backupMnemonic = SingleLiveEvent<Unit>()
    val backupKey = SingleLiveEvent<Unit>()
    val openBackupMnemonic = MutableLiveData<Intent>()
    val openBackupKey = MutableLiveData<Intent>()

    private var walletId = -1

    fun setWalletId(value: Int) {
        walletId = value
        uiScope.launch {
            withContext(Dispatchers.IO) {
                wallet.postValue(AppDatabase.getInstance().walletDao().getWalletById(value))
            }
        }
    }

    fun onPasswordPromptClick() {
        val value = wallet.value ?: return
        showPasswordPrompt.value = value.passwordPrompt
    }

    fun onBackupMnemonicClick() {
        backupMnemonic.call()
    }

    fun onBackupKeyClick() {
        backupKey.call()
    }

    fun backupMnemonic(it: String) {
        openBackupMnemonic.value = Intent().apply {
            putExtra("walletId", walletId)
            putExtra("password", it)
        }
    }

    fun backupKey(it: String) {
        openBackupKey.value = Intent().apply {
            putExtra("walletId", walletId)
            putExtra("password", it)
        }
    }
}

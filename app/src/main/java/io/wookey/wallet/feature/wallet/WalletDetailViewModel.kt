package io.wookey.wallet.feature.wallet

import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.core.XMRRepository
import io.wookey.wallet.core.XMRWalletController
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.Wallet
import io.wookey.wallet.support.viewmodel.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletDetailViewModel : BaseViewModel() {

    val wallet = MutableLiveData<Wallet>()
    val addressSetting = SingleLiveEvent<Unit>()
    val showPasswordPrompt = MutableLiveData<String>()
    val backupMnemonic = SingleLiveEvent<Unit>()
    val backupKey = SingleLiveEvent<Unit>()
    val deleteWallet = SingleLiveEvent<Unit>()
    val openAddressSetting = MutableLiveData<Intent>()
    val openBackupMnemonic = MutableLiveData<Intent>()
    val openBackupKey = MutableLiveData<Intent>()

    val showLoading = MutableLiveData<Boolean>()
    val hideLoading = MutableLiveData<Boolean>()
    val toast = MutableLiveData<String>()
    val toastRes = MutableLiveData<Int>()
    val finish = SingleLiveEvent<Boolean>()
    val restart = SingleLiveEvent<Boolean>()

    private var walletId = -1

    private val repository = XMRRepository()

    fun setWalletId(value: Int) {
        walletId = value
        uiScope.launch {
            withContext(Dispatchers.IO) {
                wallet.postValue(AppDatabase.getInstance().walletDao().getWalletById(value))
            }
        }
    }

    fun onAddressSettingClick() {
        addressSetting.call()
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

    fun addressSetting(it: String) {
        openAddressSetting.value = Intent().apply {
            putExtra("walletId", walletId)
            putExtra("password", it)
        }
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

    fun onDeleteClick() {
        deleteWallet.call()
    }

    fun deleteWallet() {
        showLoading.value = true
        uiScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val wallet = AppDatabase.getInstance().walletDao().getWalletById(walletId)
                    if (wallet == null) {
                        toastRes.postValue(R.string.data_exception)
                        return@withContext
                    }
                    // 停止钱包
                    XMRWalletController.stopWallet()
                    if (repository.deleteWallet(wallet.name)) {
                        // 删除交易记录
                        val info = AppDatabase.getInstance().transactionInfoDao().getTransactionInfoByWalletId(walletId)
                        AppDatabase.getInstance().transactionInfoDao().deleteTransactionInfo(*info.toTypedArray())
                        // 删除资产
                        val assets = AppDatabase.getInstance().assetDao().getAssetsByWalletId(walletId)
                        AppDatabase.getInstance().assetDao().deleteAssets(*assets.toTypedArray())
                        // 删除钱包
                        AppDatabase.getInstance().walletDao().deleteWallets(wallet)

                        val wallets = AppDatabase.getInstance().walletDao().getWallets()
                        if (wallet.isActive && wallets.isNotEmpty()) {
                            AppDatabase.getInstance().walletDao().updateWallets(wallets[0].apply { isActive = true })
                        }
                        toastRes.postValue(R.string.delete_success)
                        hideLoading.postValue(true)
                        if (wallets.isNotEmpty()) {
                            finish.postValue(true)
                        } else {
                            restart.postValue(true)
                        }
                    } else {
                        toastRes.postValue(R.string.delete_failed)
                        hideLoading.postValue(true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                toast.postValue(e.message)
                hideLoading.postValue(true)
            }
        }
    }
}

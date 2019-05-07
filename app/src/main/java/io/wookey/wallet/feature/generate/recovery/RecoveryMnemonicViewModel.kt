package io.wookey.wallet.feature.generate.recovery

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.core.XMRRepository
import io.wookey.wallet.core.XMRWalletController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecoveryMnemonicViewModel : BaseViewModel() {

    val enabled = MediatorLiveData<Boolean>()
    val navigation = MutableLiveData<Boolean>()
    val mnemonic = MutableLiveData<String>()
    val transactionDate = MutableLiveData<String>()

    private var blockHeight = 0L

    val showLoading = MutableLiveData<Boolean>()
    val hideLoading = MutableLiveData<Boolean>()
    val toast = MutableLiveData<String>()

    val blockHeightError = MutableLiveData<Boolean>()

    val showDialog = MutableLiveData<Boolean>()

    private val repository = XMRRepository()

    private lateinit var walletName: String
    private lateinit var password: String
    private var passwordPrompt: String? = null

    init {
        enabled.addSource(mnemonic) {
            enabled.value = checkValid()
        }
    }

    private fun checkValid(): Boolean {
        if (mnemonic.value.isNullOrBlank()) {
            return false
        }
        val error1 = blockHeightError.value
        if (error1 != null && error1) {
            return false
        }
        return true
    }

    fun initData(walletName: String, password: String, passwordPrompt: String?) {
        this.walletName = walletName
        this.password = password
        this.passwordPrompt = passwordPrompt
    }

    fun setBlockHeight(value: String) {
        if (value.isNullOrBlank()) {
            blockHeight = 0
            blockHeightError.value = null
        } else {
            try {
                val l = value.toLong()
                if (l < 0) {
                    blockHeight = 0
                    blockHeightError.value = true
                } else {
                    blockHeight = l
                    blockHeightError.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                blockHeight = 0
                blockHeightError.value = true
            }
        }
    }

    fun setTransactionDate(value: String) {
        transactionDate.value = value
        try {
            if (blockHeight == 0L) {
                blockHeight = XMRWalletController.getBlockHeight(value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun next() {
        enabled.value = false
        if (blockHeight <= 0L) {
            showDialog.value = true
            enabled.value = true
            return
        }
        create()
    }

    fun create() {
        enabled.value = false
        showLoading.postValue(true)
        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val wallet = repository.recoveryWallet(walletName, password, mnemonic.value!!, blockHeight)
                    wallet!!.passwordPrompt = passwordPrompt ?: ""
                    repository.saveWallet(wallet)
                    hideLoading.postValue(true)
                    navigation.postValue(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    repository.deleteWallet(walletName)
                    hideLoading.postValue(true)
                    toast.postValue(e.message)
                } finally {
                    enabled.postValue(true)
                }
            }
        }
    }
}
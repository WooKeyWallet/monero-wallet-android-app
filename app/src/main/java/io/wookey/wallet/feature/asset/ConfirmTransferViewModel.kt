package io.wookey.wallet.feature.asset

import android.arch.lifecycle.MutableLiveData
import android.os.SystemClock
import io.wookey.wallet.ActivityStackManager
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.core.XMRWalletController
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConfirmTransferViewModel : BaseViewModel() {

    val amount = MutableLiveData<String>()
    val fee = MutableLiveData<String>()
    val enabled = MutableLiveData<Boolean>()

    var activeWallet: Wallet? = null

    val toast = MutableLiveData<String>()
    val toastInt = MutableLiveData<Int>()

    init {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                activeWallet = AppDatabase.getInstance().walletDao().getActiveWallet()
                amount.postValue(XMRWalletController.getTxAmount())
                fee.postValue(XMRWalletController.getTxFee())
                if (activeWallet == null) {
                    toastInt.postValue(R.string.data_exception)
                    enabled.postValue(false)
                } else {
                    enabled.postValue(true)
                }
            }
        }
    }

    fun next() {
        enabled.postValue(false)
        uiScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    XMRWalletController.sendTransaction()
                    SystemClock.sleep(300)
                    if (ActivityStackManager.getInstance().contain(AssetDetailActivity::class.java)) {
                        ActivityStackManager.getInstance().finishToActivity(AssetDetailActivity::class.java)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                toast.postValue(e.message)
            } finally {
                enabled.postValue(true)
            }
        }
    }
}
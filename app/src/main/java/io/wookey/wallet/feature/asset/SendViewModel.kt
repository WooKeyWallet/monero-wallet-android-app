package io.wookey.wallet.feature.asset

import android.app.Activity
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.core.XMRWalletController
import io.wookey.wallet.data.entity.Asset
import io.wookey.wallet.data.entity.Wallet
import io.wookey.wallet.support.REQUEST_SCAN_ADDRESS
import io.wookey.wallet.support.REQUEST_SELECT_ADDRESS
import io.wookey.wallet.support.extensions.formatterAmountStrip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SendViewModel : BaseViewModel() {

    val receiveAddress = MutableLiveData<String>()
    val receiveAmount = MutableLiveData<String>()
    var paymentId = MutableLiveData<String>()
    val enabled = MediatorLiveData<Boolean>()

    val autoFillAddress = MutableLiveData<String>()
    val autoFillAmount = MutableLiveData<String>()

    val autoFillPaymentId = MutableLiveData<String>()
    var activeWallet: Wallet? = null
    var activeAsset: Asset? = null

    val selectAddress = MutableLiveData<String>()
    val counterMaxLength = MutableLiveData<Int>()
    var isAll = false

    var confirmTransfer = MutableLiveData<Intent>()

    val showLoading = MutableLiveData<Boolean>()
    val hideLoading = MutableLiveData<Boolean>()
    val toast = MutableLiveData<String>()

    val addressError = MutableLiveData<Boolean>()
    val paymentIdError = MutableLiveData<Boolean>()

    init {
        enabled.addSource(receiveAddress) {
            enabled.value = checkValid()
        }
        enabled.addSource(receiveAmount) {
            enabled.value = checkValid()
        }
        enabled.addSource(paymentId) {
            enabled.value = checkValid()
        }
    }

    private fun checkValid(): Boolean {
        if (receiveAddress.value.isNullOrBlank()) {
            return false
        }
        val error1 = addressError.value
        if (error1 != null && error1) {
            return false
        }
        if (receiveAmount.value.isNullOrBlank()) {
            return false
        }
        val error2 = paymentIdError.value
        if (error2 != null && error2) {
            return false
        }
        return true
    }

    fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_SCAN_ADDRESS || requestCode == REQUEST_SELECT_ADDRESS) {
            data?.getStringExtra("result")?.let {
                if (it.isNotBlank()) {
                    autoFillAddress.value = it
                }
            }
        }
    }

    fun addressChanged(it: String) {
        if (it.isNullOrBlank()) {
            addressError.value = null
        } else {
            addressError.value = !XMRWalletController.isAddressValid(it)
        }
        receiveAddress.value = it
    }

    fun amountChanged(it: String) {
        receiveAmount.value = it
        isAll = autoFillAmount.value == it
    }

    fun paymentIdChanged(it: String) {
        if (it.isNullOrBlank()) {
            paymentIdError.value = null
        } else {
            paymentIdError.value = !XMRWalletController.isPaymentIdValid(it)
        }
        paymentId.value = it
        if (it.length > 16) {
            counterMaxLength.value = 64
        } else {
            counterMaxLength.value = 16
        }
    }

    fun clickAddressBook() {
        activeWallet?.let {
            selectAddress.value = it.symbol
        }
    }

    fun clickAll() {
        activeAsset?.let {
            autoFillAmount.value = it.balance.formatterAmountStrip()
        }
    }

    fun clickGenerate() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                autoFillPaymentId.postValue(XMRWalletController.generatePaymentId())
            }
        }
    }

    fun next() {
        enabled.postValue(false)
        showLoading.postValue(true)
        uiScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    XMRWalletController.createTransaction(isAll, receiveAddress.value!!, paymentId.value, receiveAmount.value!!)
                    confirmTransfer.postValue(Intent().apply {
                        putExtra("token", activeAsset?.token)
                        putExtra("address", receiveAddress.value)
                        putExtra("paymentID", paymentId.value)
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
                toast.postValue(e.message)
            } finally {
                enabled.postValue(true)
                hideLoading.postValue(true)
            }
        }
    }

}
package io.wookey.wallet.feature.asset

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.graphics.Bitmap
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.core.XMRWalletController
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.Asset
import io.wookey.wallet.data.entity.Wallet
import io.wookey.wallet.support.REQUEST_SELECT_SUB_ADDRESS
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.viewmodel.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReceiveViewModel : BaseViewModel() {

    val activeAsset = MutableLiveData<Asset>()
    val activeWallet = MutableLiveData<Wallet>()
    val label = MutableLiveData<String>()
    val address = MutableLiveData<String>()
    val visibilityIcon = MutableLiveData<Int>()
    var addressVisibility = true

    val QRCodeBitmap = MutableLiveData<Bitmap>()
    val toast = MutableLiveData<Int>()

    var isCollapsing = true
    val moreOptions = SingleLiveEvent<Unit>()
    val collapsingOptions = SingleLiveEvent<Unit>()

    val paymentId = MutableLiveData<String>()
    val integratedAddress = MutableLiveData<String>()

    val paymentIdError = MutableLiveData<Int>()
    val integratedError = MutableLiveData<Int>()

    fun setAssetId(assetId: Int) {
        uiScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val asset = AppDatabase.getInstance().assetDao().getAssetById(assetId)
                        ?: throw IllegalStateException()
                    activeAsset.postValue(asset)
                    val wallet = AppDatabase.getInstance().walletDao().getActiveWallet()
                        ?: throw IllegalStateException()
                    activeWallet.postValue(wallet)
                    val wAddress = wallet.address
                    address.postValue(wAddress)
                    label.postValue(XMRWalletController.getLabelByAddress(wAddress))
                    if (XMRWalletController.isAddressValid(wAddress)) {
                        QRCodeBitmap.postValue(QRCodeEncoder.syncEncodeQRCode(wAddress, dp2px(115)))
                    } else {
                        QRCodeBitmap.postValue(null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                toast.postValue(R.string.data_exception)
            }
        }
    }

    fun setAddressVisible() {
        val addressStr = activeWallet.value?.address ?: ""
        if (addressStr.isBlank()) {
            return
        }
        addressVisibility = !addressVisibility
        if (addressVisibility) {
            address.value = addressStr
            visibilityIcon.value = R.drawable.icon_visible_space
        } else {
            visibilityIcon.value = R.drawable.icon_invisible_space
            val str = StringBuilder()
            addressStr.forEach {
                str.append("*")
            }
            address.value = str.toString()
        }
    }

    fun more() {
        isCollapsing = !isCollapsing
        if (isCollapsing) {
            collapsingOptions.call()
        } else {
            moreOptions.call()
        }
    }

    fun generate() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                paymentId.postValue(XMRWalletController.generatePaymentId())
            }
        }
    }

    fun paymentIdChanged(id: String) {
        if (id.isNullOrBlank()) {
            integratedAddress.value = ""
            return
        }
        uiScope.launch {
            withContext(Dispatchers.IO) {
                if (id.length == 16 || id.length == 64) {
                    if (XMRWalletController.isPaymentIdValid(id)) {
                        integratedAddress.postValue(XMRWalletController.getIntegratedAddress(id))
                        paymentIdError.postValue(null)
                    } else {
                        integratedAddress.postValue("")
                        paymentIdError.postValue(R.string.payment_id_invalid)
                    }
                } else {
                    integratedAddress.postValue("")
                }
            }
        }
    }

    fun integratedChanged(value: String) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                var address = value
                if (!address.isNullOrBlank()) {
                    if (XMRWalletController.isAddressValid(address)) {
                        integratedError.postValue(null)
                    } else {
                        integratedError.postValue(R.string.integrated_invalid)
                    }
                } else {
                    address = activeWallet.value?.address ?: ""
                    integratedError.postValue(null)
                }
                if (XMRWalletController.isAddressValid(address)) {
                    QRCodeBitmap.postValue(QRCodeEncoder.syncEncodeQRCode(address, dp2px(115)))
                } else {
                    QRCodeBitmap.postValue(null)
                }
            }
        }
    }

    fun generateQRCode(address: String) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                if (XMRWalletController.isAddressValid(address)) {
                    QRCodeBitmap.postValue(QRCodeEncoder.syncEncodeQRCode(address, dp2px(115)))
                } else {
                    QRCodeBitmap.postValue(null)
                }
            }
        }
    }

    fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_SELECT_SUB_ADDRESS -> {
                val subAddress = data?.getStringExtra("subAddress") ?: return
                address.value = subAddress
                generateQRCode(subAddress)
            }
        }
    }

}
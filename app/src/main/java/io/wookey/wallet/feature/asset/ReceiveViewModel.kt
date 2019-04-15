package io.wookey.wallet.feature.asset

import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.core.XMRWalletController
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.Asset
import io.wookey.wallet.data.entity.Wallet
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.viewmodel.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReceiveViewModel : BaseViewModel() {

    val activeAsset = MutableLiveData<Asset>()
    val activeWallet = MutableLiveData<Wallet>()

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
                    if (XMRWalletController.isAddressValid(wallet.address)) {
                        QRCodeBitmap.postValue(QRCodeEncoder.syncEncodeQRCode(wallet.address, dp2px(115)))
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

}
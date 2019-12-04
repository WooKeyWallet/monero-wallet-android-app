package io.wookey.wallet.feature.asset

import android.content.Intent
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.data.entity.Asset
import io.wookey.wallet.data.entity.Wallet
import io.wookey.wallet.support.extensions.putBoolean
import io.wookey.wallet.support.extensions.sharedPreferences
import io.wookey.wallet.support.viewmodel.SingleLiveEvent

class AssetViewModel : BaseViewModel() {

    val showPasswordDialog = SingleLiveEvent<Unit>()
    val openAssetDetail = SingleLiveEvent<Intent>()

    var wallet: Wallet? = null

    val assetVisible = SingleLiveEvent<Unit>()
    val assetInvisible = SingleLiveEvent<Unit>()

    private var asset: Asset? = null

    fun initVisible() {
        val visible = sharedPreferences().getBoolean("assetVisible", true)
        if (visible) {
            assetVisible.call()
        } else {
            assetInvisible.call()
        }
    }

    fun onItemClick(value: Asset) {
        asset = value
        showPasswordDialog.call()
    }

    fun next(password: String) {
        if (asset != null) {
            openAssetDetail.value = Intent().apply {
                putExtra("password", password)
                putExtra("assetId", asset!!.id)
            }
            asset = null
        }
    }

    fun assetVisibleChanged() {
        val visible = sharedPreferences().getBoolean("assetVisible", true)
        sharedPreferences().putBoolean("assetVisible", !visible)
        if (!visible) {
            assetVisible.call()
        } else {
            assetInvisible.call()
        }
    }
}
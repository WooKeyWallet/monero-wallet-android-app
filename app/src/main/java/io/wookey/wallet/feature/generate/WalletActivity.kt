package io.wookey.wallet.feature.generate

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseActivity
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.WALLET_CREATE
import io.wookey.wallet.support.WALLET_RECOVERY
import io.wookey.wallet.support.extensions.putInt
import io.wookey.wallet.support.extensions.putString
import io.wookey.wallet.support.extensions.sharedPreferences
import io.wookey.wallet.support.utils.StatusBarHelper
import kotlinx.android.synthetic.main.activity_wallet.*

class WalletActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        StatusBarHelper.translucent(this, ContextCompat.getColor(this, R.color.color_FFFFFF))
        StatusBarHelper.setStatusBarLightMode(this)

        createWallet.background = BackgroundHelper.getButtonBackground(this, R.color.color_00A761)
        recoveryWallet.background = BackgroundHelper.getButtonBackground(this, R.color.color_002C6D)

        sharedPreferences().putString("symbol", "XMR")
        createWallet.setOnClickListener {
            startActivity(Intent(this, GenerateWalletActivity::class.java).apply {
                sharedPreferences().putInt("type", WALLET_CREATE)
            })
        }
        recoveryWallet.setOnClickListener {
            startActivity(Intent(this, GenerateWalletActivity::class.java).apply {
                sharedPreferences().putInt("type", WALLET_RECOVERY)
            })
        }
    }
}

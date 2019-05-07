package io.wookey.wallet

import android.content.Intent
import android.os.Bundle
import io.wookey.wallet.base.BaseActivity
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.feature.generate.WalletActivity
import io.wookey.wallet.feature.generate.create.BackupMnemonicActivity
import io.wookey.wallet.support.extensions.sharedPreferences
import kotlinx.coroutines.*

class SplashActivity : BaseActivity() {

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        uiScope.launch {
            delay(1500)
            val walletId = sharedPreferences().getInt("walletId", -1)
            val activeWallet = withContext(Dispatchers.IO) {
                val wallets = AppDatabase.getInstance().walletDao().getWallets()
                if (wallets.isNullOrEmpty()) {
                    null
                } else {
                    val activeWallets = AppDatabase.getInstance().walletDao().getActiveWallets()
                    if (activeWallets.isNullOrEmpty()) {
                        AppDatabase.getInstance().walletDao().updateWallets(wallets[0].apply { isActive = true })
                        wallets[0]
                    } else {
                        activeWallets.forEachIndexed { index, wallet ->
                            if (index > 0) {
                                AppDatabase.getInstance().walletDao().updateWallets(wallet.apply { isActive = false })
                            }
                        }
                        activeWallets[0]
                    }
                }
            }
            val walletAddress = activeWallet?.address
            navigation(walletId, walletAddress)
        }
    }

    private fun navigation(walletId: Int, address: String?) {
        if (walletId >= 0) {
            startActivity(Intent(this, BackupMnemonicActivity::class.java).apply {
                putExtra("walletId", walletId)
            })
        } else if (!address.isNullOrBlank()) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, WalletActivity::class.java))
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

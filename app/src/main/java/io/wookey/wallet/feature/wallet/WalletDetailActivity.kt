package io.wookey.wallet.feature.wallet

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.dialog.PasswordDialog
import io.wookey.wallet.dialog.PasswordPromptDialog
import io.wookey.wallet.feature.generate.WalletActivity
import io.wookey.wallet.feature.generate.create.BackupMnemonicActivity
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.copy
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.formatterAmountStrip
import io.wookey.wallet.support.extensions.toast
import kotlinx.android.synthetic.main.activity_wallet_detail.*

class WalletDetailActivity : BaseTitleSecondActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_detail)

        val walletId = intent.getIntExtra("walletId", -1)
        if (walletId < 0) {
            finish()
            return
        }
        val viewModel = ViewModelProviders.of(this).get(WalletDetailViewModel::class.java)
        viewModel.setWalletId(walletId)

        viewModel.wallet.observe(this, Observer { value ->
            value?.let {
                setCenterTitle(it.name)
                assetTitle.text = getString(R.string.asset_placeholder, it.symbol)
                asset.text = it.balance.formatterAmountStrip()
                address.rightTextView.ellipsize = TextUtils.TruncateAt.MIDDLE
                address.rightTextView.maxEms = 10
                address.rightTextView.setSingleLine()
                address.setRightString(it.address)
                walletName.setRightString(it.name)
            }
        })

        AppDatabase.getInstance().walletDao().loadWalletById(walletId).observe(this, Observer { value ->
            value?.let {
                address.setRightString(it.address)
            }
        })

        address.setOnClickListener { viewModel.onAddressSettingClick() }
        passwordPrompt.setOnClickListener { viewModel.onPasswordPromptClick() }
        backupMnemonic.setOnClickListener { viewModel.onBackupMnemonicClick() }
        backupKey.setOnClickListener { viewModel.onBackupKeyClick() }
        delete.setOnClickListener { viewModel.onDeleteClick() }

        viewModel.addressSetting.observe(this, Observer {
            PasswordDialog.display(supportFragmentManager, walletId) {
                viewModel.addressSetting(it)
            }
        })

        viewModel.showPasswordPrompt.observe(this, Observer { value ->
            value?.let {
                PasswordPromptDialog.display(supportFragmentManager, it)
            }
        })

        viewModel.backupMnemonic.observe(this, Observer {
            PasswordDialog.display(supportFragmentManager, walletId) {
                viewModel.backupMnemonic(it)
            }
        })

        viewModel.backupKey.observe(this, Observer {
            PasswordDialog.display(supportFragmentManager, walletId) {
                viewModel.backupKey(it)
            }
        })

        viewModel.openAddressSetting.observe(this, Observer { value ->
            value?.let {
                startActivity(it.apply {
                    setClass(this@WalletDetailActivity, AddressSettingActivity::class.java)
                })
            }
        })

        viewModel.openBackupMnemonic.observe(this, Observer { value ->
            value?.let {
                startActivity(it.apply {
                    setClass(this@WalletDetailActivity, BackupMnemonicActivity::class.java)
                })
            }
        })

        viewModel.openBackupKey.observe(this, Observer { value ->
            value?.let {
                startActivity(it.apply {
                    setClass(this@WalletDetailActivity, BackupKeyActivity::class.java)
                })
            }
        })

        viewModel.deleteWallet.observe(this, Observer {
            PasswordDialog.display(supportFragmentManager, walletId) { value ->
                viewModel.deleteWallet()
            }
        })

        viewModel.showLoading.observe(this, Observer { showLoading() })
        viewModel.hideLoading.observe(this, Observer { hideLoading() })

        viewModel.toast.observe(this, Observer { toast(it) })
        viewModel.toastRes.observe(this, Observer { toast(it) })
        viewModel.finish.observe(this, Observer { finish() })
        viewModel.restart.observe(this, Observer {
            startActivity(Intent(this, WalletActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
            finish()
        })
    }
}
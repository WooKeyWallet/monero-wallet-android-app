package io.wookey.wallet.feature.wallet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.dialog.PasswordDialog
import io.wookey.wallet.dialog.PasswordPromptDialog
import io.wookey.wallet.feature.auth.AuthManager
import io.wookey.wallet.feature.generate.WalletActivity
import io.wookey.wallet.feature.generate.create.BackupMnemonicActivity
import io.wookey.wallet.support.REQUEST_PATTERN_CHECKING
import io.wookey.wallet.support.REQUEST_PATTERN_CHECKING_ADDRESS_SETTING
import io.wookey.wallet.support.REQUEST_PATTERN_CHECKING_BACKUP_KEY
import io.wookey.wallet.support.REQUEST_PATTERN_CHECKING_BACKUP_MNEMONIC
import io.wookey.wallet.support.extensions.formatterAmountStrip
import io.wookey.wallet.support.extensions.toast
import kotlinx.android.synthetic.main.activity_wallet_detail.*

class WalletDetailActivity : BaseTitleSecondActivity() {

    private lateinit var viewModel: WalletDetailViewModel
    private var walletId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_detail)

        walletId = intent.getIntExtra("walletId", -1)
        if (walletId < 0) {
            finish()
            return
        }
        viewModel = ViewModelProviders.of(this).get(WalletDetailViewModel::class.java)
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

        biological.setOnClickListener { viewModel.onBiologicalClick() }

        viewModel.addressSetting.observe(this, Observer { _ ->
            AuthManager(viewModel.walletRelease, walletId).backup(this, requestCode = REQUEST_PATTERN_CHECKING_ADDRESS_SETTING) { value ->
                value?.let {
                    viewModel.addressSetting(it)
                }
            }
        })

        viewModel.showPasswordPrompt.observe(this, Observer { value ->
            value?.let {
                PasswordPromptDialog.display(supportFragmentManager, it)
            }
        })

        viewModel.backupMnemonic.observe(this, Observer { _ ->
            AuthManager(viewModel.walletRelease, walletId).backup(this, requestCode = REQUEST_PATTERN_CHECKING_BACKUP_MNEMONIC) { value ->
                value?.let {
                    viewModel.backupMnemonic(it)
                }
            }
        })

        viewModel.backupKey.observe(this, Observer { _ ->
            AuthManager(viewModel.walletRelease, walletId).backup(this, requestCode = REQUEST_PATTERN_CHECKING_BACKUP_KEY) { value ->
                value?.let {
                    viewModel.backupKey(it)
                }
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

        viewModel.biological.observe(this, Observer {
            PasswordDialog.display(supportFragmentManager, walletId) { password ->
                startActivity(Intent(this, ReleaseModeActivity::class.java).apply {
                    putExtra("password", password)
                    putExtra("walletId", walletId)
                })
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

    override fun onResume() {
        super.onResume()
        viewModel.setWalletId(walletId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_PATTERN_CHECKING_ADDRESS_SETTING -> {
                data?.getStringExtra("password")?.let {
                    viewModel.addressSetting(it)
                }
            }
            REQUEST_PATTERN_CHECKING_BACKUP_MNEMONIC -> {
                data?.getStringExtra("password")?.let {
                    viewModel.backupMnemonic(it)
                }
            }
            REQUEST_PATTERN_CHECKING_BACKUP_KEY -> {
                data?.getStringExtra("password")?.let {
                    viewModel.backupKey(it)
                }
            }
        }
    }
}
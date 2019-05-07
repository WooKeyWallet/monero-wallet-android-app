package io.wookey.wallet.feature.wallet

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.support.extensions.copy
import io.wookey.wallet.support.extensions.toast
import kotlinx.android.synthetic.main.activity_backup_key.*

class BackupKeyActivity : BaseTitleSecondActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_backup_key)
        setCenterTitle(R.string.backup_key)

        val walletId = intent.getIntExtra("walletId", -1)
        val password = intent.getStringExtra("password")
        if (walletId < 0 || password.isNullOrBlank()) {
            finish()
            return
        }
        val viewModel = ViewModelProviders.of(this).get(BackupKeyViewModel::class.java)
        viewModel.openWallet(walletId, password)

        publicViewKey.setOnClickListener { copy(publicViewKey.text.toString()) }
        secretViewKey.setOnClickListener { copy(secretViewKey.text.toString()) }
        publicSpendKey.setOnClickListener { copy(publicSpendKey.text.toString()) }
        secretSpendKey.setOnClickListener { copy(secretSpendKey.text.toString()) }
        address.setOnClickListener { copy(address.text.toString()) }

        viewModel.publicViewKey.observe(this, Observer { value -> value?.let { publicViewKey.text = it } })
        viewModel.secretViewKey.observe(this, Observer { value -> value?.let { secretViewKey.text = it } })
        viewModel.publicSpendKey.observe(this, Observer { value -> value?.let { publicSpendKey.text = it } })
        viewModel.secretSpendKey.observe(this, Observer { value -> value?.let { secretSpendKey.text = it } })
        viewModel.address.observe(this, Observer { value -> value?.let { address.text = it } })

        viewModel.showLoading.observe(this, Observer { showLoading() })
        viewModel.hideLoading.observe(this, Observer { hideLoading() })

        viewModel.toast.observe(this, Observer { toast(it) })
        viewModel.toastRes.observe(this, Observer { toast(it) })
    }
}
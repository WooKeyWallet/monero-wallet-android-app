package io.wookey.wallet.feature.asset

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.View
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.feature.address.AddressBookActivity
import io.wookey.wallet.feature.address.ScanActivity
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.REQUEST_SCAN_ADDRESS
import io.wookey.wallet.support.REQUEST_SELECT_ADDRESS
import io.wookey.wallet.support.extensions.afterTextChanged
import io.wookey.wallet.support.extensions.formatterAmountStrip
import io.wookey.wallet.support.extensions.setImage
import io.wookey.wallet.support.extensions.toast
import kotlinx.android.synthetic.main.activity_send.*

class SendActivity : BaseTitleSecondActivity() {

    private lateinit var viewModel: SendViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        val assetId = intent.getIntExtra("assetId", -1)
        if (assetId == -1) {
            finish()
            return
        }

        viewModel = ViewModelProviders.of(this).get(SendViewModel::class.java)

        setRightIcon(R.drawable.icon_scan)
        setRightIconClick(View.OnClickListener { scanAddress() })

        addressBook.setOnClickListener { viewModel.clickAddressBook() }
        all.setOnClickListener { viewModel.clickAll() }
        generate.setOnClickListener { viewModel.clickGenerate() }

        address.editText?.afterTextChanged { viewModel.addressChanged(it) }
        amount.editText?.afterTextChanged { viewModel.amountChanged(it) }
        paymentID.editText?.afterTextChanged { viewModel.paymentIdChanged(it) }

        viewModel.counterMaxLength.observe(this, Observer { value ->
            value?.let {
                paymentID.counterMaxLength = it
            }
        })

        viewModel.autoFillAddress.observe(this, Observer { value ->
            value?.let {
                address.editText?.setText(it)
                address.editText?.setSelection(it.length)
            }
        })
        viewModel.autoFillAmount.observe(this, Observer { value ->
            value?.let {
                amount.editText?.setText(it)
                amount.editText?.setSelection(it.length)
            }
        })
        viewModel.autoFillPaymentId.observe(this, Observer { value ->
            value?.let {
                paymentID.editText?.setText(it)
                paymentID.editText?.setSelection(it.length)
            }
        })

        next.background = BackgroundHelper.getButtonBackground(this)
        next.setOnClickListener {
            viewModel.next()
        }
        viewModel.enabled.observe(this, Observer { value ->
            value?.let {
                next.isEnabled = it
            }
        })

        viewModel.selectAddress.observe(this, Observer { value ->
            value?.let {
                selectAddress(it)
            }
        })

        viewModel.confirmTransfer.observe(this, Observer { value ->
            value?.let {
                confirmTransfer(it)
            }
        })

        viewModel.showLoading.observe(this, Observer {
            showLoading()
        })

        viewModel.hideLoading.observe(this, Observer {
            hideLoading()
        })

        viewModel.toast.observe(this, Observer { toast(it) })

        viewModel.addressError.observe(this, Observer {
            if (it != null && it) {
                address.error = getString(R.string.address_invalid)
            } else {
                address.error = null
            }
        })

        viewModel.paymentIdError.observe(this, Observer {
            if (it != null && it) {
                paymentID.error = getString(R.string.payment_id_invalid)
            } else {
                paymentID.error = null
            }
        })

        AppDatabase.getInstance().walletDao().loadActiveWallet().observe(this, Observer { value ->
            value?.let {
                walletName.text = it.name
                viewModel.activeWallet = it
            }
        })

        AppDatabase.getInstance().assetDao().loadAssetById(assetId).observe(this, Observer { value ->
            value?.let {
                setCenterTitle("${it.token} ${getString(R.string.account_send)}")
                icon.setImage(it.token)
                balance.text = "${it.balance.formatterAmountStrip()} ${it.token}"
                viewModel.activeAsset = it
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.handleResult(requestCode, resultCode, data)
    }

    private fun scanAddress() {
        startActivityForResult(Intent(this, ScanActivity::class.java), REQUEST_SCAN_ADDRESS)
    }

    private fun selectAddress(symbol: String) {
        startActivityForResult(Intent(this, AddressBookActivity::class.java).apply {
            putExtra("symbol", symbol)
        }, REQUEST_SELECT_ADDRESS)
    }

    private fun confirmTransfer(intent: Intent) {
        startActivity(intent.apply {
            setClass(this@SendActivity, ConfirmTransferActivity::class.java)
        })
    }
}
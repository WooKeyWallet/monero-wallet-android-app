package io.wookey.wallet.feature.asset

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.View
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.feature.wallet.AddressSettingActivity
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.REQUEST_SELECT_SUB_ADDRESS
import io.wookey.wallet.support.extensions.afterTextChanged
import io.wookey.wallet.support.extensions.copy
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.toast
import kotlinx.android.synthetic.main.activity_receive.*

class ReceiveActivity : BaseTitleSecondActivity() {

    lateinit var viewModel: ReceiveViewModel
    var assetId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive)

        val password = intent.getStringExtra("password")
        assetId = intent.getIntExtra("assetId", -1)

        if (password.isNullOrBlank()) {
            finish()
            return
        }

        if (assetId == -1) {
            finish()
            return
        }
        viewModel = ViewModelProviders.of(this).get(ReceiveViewModel::class.java)
        viewModel.setAssetId(assetId)

        divider.background = BackgroundHelper.getDashDrawable(this)
        addressBg.background = BackgroundHelper.getBackground(this, R.color.color_F3F4F6, dp2px(5))
        amount.background = BackgroundHelper.getEditBackground(this)
        paymentIDBg.background = BackgroundHelper.getBackground(this, R.color.color_F3F4F6, dp2px(5))
        integratedBg.background = BackgroundHelper.getBackground(this, R.color.color_F3F4F6, dp2px(5))

        viewModel.activeAsset.observe(this, Observer { value ->
            value?.let {
                setCenterTitle("${it.token} ${getString(R.string.account_receive)}")
                contentTitle.text = "${getString(R.string.please_transfer_in)} ${it.token}"
                prompt.text = getString(R.string.receive_prompt, it.token)
            }
        })

        viewModel.label.observe(this, Observer { value ->
            label.text = value ?: getString(R.string.no_label)
        })

        viewModel.address.observe(this, Observer { value ->
            address.text = value ?: ""
        })

        viewModel.visibilityIcon.observe(this, Observer { value ->
            value?.let {
                visible.setImageResource(it)
            }
        })

        visible.setOnClickListener {
            viewModel.setAddressVisible()
        }

        viewModel.QRCodeBitmap.observe(this, Observer {
            QRCode.setImageBitmap(it)
        })

        viewModel.toast.observe(this, Observer { value ->
            value?.let {
                toast(it)
            }
        })

        more.setOnClickListener {
            viewModel.more()
        }

        switchAddress.setOnClickListener {
            startActivityForResult(Intent(this, AddressSettingActivity::class.java).apply {
                putExtra("password", password)
            }, REQUEST_SELECT_SUB_ADDRESS)
        }

        copyAddress.setOnClickListener { copy(address.text.toString()) }
        copyPaymentID.setOnClickListener { copy(paymentID.text.toString()) }
        copyIntegrated.setOnClickListener { copy(integrated.text.toString()) }

        viewModel.moreOptions.observe(this, Observer {
            moreOptions.visibility = View.VISIBLE
            more.text = getString(R.string.collapsing_options)
            more.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.arrow_up, 0)
        })

        viewModel.collapsingOptions.observe(this, Observer {
            moreOptions.visibility = View.GONE
            more.text = getString(R.string.more_options)
            more.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0)
        })

        generate.setOnClickListener {
            viewModel.generate()
        }

        viewModel.paymentId.observe(this, Observer { value ->
            value?.let {
                paymentID.setText(it)
                paymentID.setSelection(it.length)
            }
        })

        paymentID.afterTextChanged {
            viewModel.paymentIdChanged(it)
        }

        viewModel.integratedAddress.observe(this, Observer { value ->
            value?.let {
                integrated.setText(it)
                integrated.setSelection(it.length)
            }
        })

        integrated.afterTextChanged {
            viewModel.integratedChanged(it)
        }

        viewModel.paymentIdError.observe(this, Observer {
            if (it == null) {
                paymentIdError.text = ""
            } else {
                paymentIdError.text = getString(it)
            }
        })
        viewModel.integratedError.observe(this, Observer {
            if (it == null) {
                integratedError.text = ""
            } else {
                integratedError.text = getString(it)
            }
        })
    }

    override fun hide(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        viewModel.setAssetId(assetId)
    }
}
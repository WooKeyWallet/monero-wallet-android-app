package io.wookey.wallet.feature.asset

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseActivity
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.feature.auth.AuthManager
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.REQUEST_PATTERN_CHECKING
import io.wookey.wallet.support.extensions.formatterAmountStrip
import io.wookey.wallet.support.extensions.setImage
import io.wookey.wallet.support.extensions.toast
import kotlinx.android.synthetic.main.activity_confirm_transfer.*

class ConfirmTransferActivity : BaseTitleSecondActivity() {

    private lateinit var viewModel: ConfirmTransferViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_transfer)
        setCenterTitle(R.string.confirm_transfer)

        val token = intent.getStringExtra("token")
        val addressValue = intent.getStringExtra("address")
        val paymentIDValue = intent.getStringExtra("paymentID")

        if (token.isNullOrBlank() || addressValue.isNullOrBlank()) {
            finish()
            return
        }

        viewModel = ViewModelProviders.of(this).get(ConfirmTransferViewModel::class.java)

        icon.setImage(token)
        address.text = addressValue
        paymentID.text = paymentIDValue ?: ""

        next.background = BackgroundHelper.getButtonBackground(this)

        next.setOnClickListener {
            val id = viewModel.activeWallet?.id ?: return@setOnClickListener
            AuthManager(viewModel.walletRelease, id).sendTransaction(this as BaseActivity) { password ->
                password?.let {
                    viewModel.next(it)
                }
            }
        }

        viewModel.amount.observe(this, Observer { value ->
            value?.let {
                amount.text = "${it.formatterAmountStrip()} $token"
            }
        })

        viewModel.fee.observe(this, Observer { value ->
            value?.let {
                fee.text = it.formatterAmountStrip()
            }
        })

        viewModel.enabled.observe(this, Observer { value ->
            value?.let {
                next.isEnabled = it
            }
        })

        viewModel.toast.observe(this, Observer { toast(it) })
        viewModel.toastInt.observe(this, Observer { toast(it) })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_PATTERN_CHECKING -> {
                data?.getStringExtra("password")?.let {
                    viewModel.next(it)
                }
            }
        }
    }
}
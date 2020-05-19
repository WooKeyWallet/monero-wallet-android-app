package io.wookey.wallet.feature.asset

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.data.entity.TransactionInfo
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.copy
import io.wookey.wallet.support.extensions.formatterAmountStrip
import io.wookey.wallet.support.extensions.formatterDate
import kotlinx.android.synthetic.main.activity_transaction_detail.*

class TransactionDetailActivity : BaseTitleSecondActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)

        val transaction = intent.getParcelableExtra("transaction") as? TransactionInfo
        if (transaction == null) {
            finish()
            return
        }

        val viewModel = ViewModelProviders.of(this).get(TransactionDetailViewModel::class.java)

        setCenterTitle("${transaction.token} ${getString(R.string.transaction_detail)}")
        divider.background = BackgroundHelper.getDashDrawable(this)

        when {
            transaction.isFailed -> {
                icon.setImageResource(R.drawable.icon_failed)
                status.text = getString(R.string.transfer_failed)
            }
            transaction.isPending -> {
                icon.setImageResource(R.drawable.icon_pending)
                status.text = getString(R.string.pending)
            }
            else -> {
                icon.setImageResource(R.drawable.icon_success)
                status.text = getString(R.string.transfer_success)
            }
        }
        time.text = transaction.timestamp.formatterDate()
        if (transaction.direction == 1) {
            direction.text = getString(R.string.send)
            addressTitle.text = getString(R.string.received_address)

            labelRow.visibility = View.GONE
        } else {
            direction.text = getString(R.string.receive)
            addressTitle.text = getString(R.string.sent_address)

            labelRow.visibility = View.VISIBLE
            if (transaction.subAddressLabel.isNullOrBlank()) {
                label.setText(R.string.no_label)
            } else {
                label.text = transaction.subAddressLabel
            }
        }
        amount.text = "${transaction.amount?.formatterAmountStrip() ?: "--"}"
        fee.text = "${transaction.fee?.formatterAmountStrip() ?: "--"}"
        paymentID.text = transaction.paymentId ?: "--"
        txId.text = transaction.hash ?: "--"
        blockHeight.text = transaction.blockHeight.toString()

        if (transaction.address.isNullOrBlank()) {
            addressRow.visibility = View.GONE
        } else {
            addressRow.visibility = View.VISIBLE
        }

        paymentID.setOnClickListener { copy(paymentID.text.toString()) }
        txId.setOnClickListener { copy(txId.text.toString()) }
        blockHeight.setOnClickListener { copy(blockHeight.text.toString()) }

        viewModel.setTxId(transaction.hash)

        viewModel.QRCodeBitmap.observe(this, Observer { value ->
            value?.let {
                QRCode.setImageBitmap(it)
            }
        })
    }
}
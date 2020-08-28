package io.wookey.wallet.feature.swap

import android.os.Bundle
import androidx.lifecycle.Observer
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.data.remote.entity.SwapTransaction
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.formatterDate
import io.wookey.wallet.support.extensions.toast
import io.wookey.wallet.support.extensions.viewModel
import kotlinx.android.synthetic.main.activity_swap_detail.*
import java.util.*

class SwapDetailActivity : BaseTitleSecondActivity() {

    val viewModel by viewModel<SwapDetailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swap_detail)

        setCenterTitle(R.string.swap_detail)

        divider.background = BackgroundHelper.getDashDrawable(this)

        val id = intent.getStringExtra("id")
        if (id.isNullOrBlank()) {
            finish()
            return
        }

        viewModel.loadData(id)

        viewModel.swapTransaction.observe(this, Observer {
            if (it != null) {
                setView(it)
            }
        })

        viewModel.showLoading.observe(this, Observer { showLoading() })
        viewModel.hideLoading.observe(this, Observer { hideLoading() })
        viewModel.toast.observe(this, Observer { toast(it) })

    }

    private fun setView(transaction: SwapTransaction) {
        when (transaction.status) {
            "finished" -> {
                icon.setImageResource(R.drawable.icon_success)
                status.text = getString(R.string.swap_success)
            }
            "failed", "refunded", "overdue" -> {
                icon.setImageResource(R.drawable.icon_failed)
                status.text = getString(R.string.swap_failed)
            }
            else -> {
                icon.setImageResource(R.drawable.icon_pending)
                status.text = getString(R.string.swap_pending)
            }
        }
        try {
            time.text = transaction.createdAt.times(1000).formatterDate()
            val none = getString(R.string.no_prompt)
            id.text = transaction.id

            val amountFrom =
                if (transaction.amountFrom.isNullOrBlank() || transaction.amountFrom.toBigDecimal() <= "0.0".toBigDecimal()) {
                    transaction.amountExpectedFrom
                } else {
                    transaction.amountFrom
                }
            fromAmount.text =
                "${amountFrom ?: "--"} ${transaction.currencyFrom.toUpperCase(Locale.CHINA)}"

            val amountTo =
                if (transaction.amountTo.isNullOrBlank() || transaction.amountTo.toBigDecimal() <= "0.0".toBigDecimal()) {
                    transaction.amountExpectedTo
                } else {
                    transaction.amountTo
                }
            toAmount.text =
                "${amountTo ?: "--"} ${transaction.currencyTo.toUpperCase(Locale.CHINA)}"

            val totalFee = if (transaction.totalFee.isNullOrBlank()) "--" else transaction.totalFee
            fee.text = "$totalFee ${transaction.currencyTo.toUpperCase(Locale.CHINA)}"

            toAddress.text = "${transaction.payoutAddress ?: "--"}"

            addressTag.text =
                "${if (transaction.addressTag.isNullOrBlank()) none else transaction.addressTag}"

            extraID.text =
                "${if (transaction.payoutExtraId.isNullOrBlank()) none else transaction.payoutExtraId}"
        } catch (e: Exception) {
            e.printStackTrace()
            toast(e.message)
        }
    }
}
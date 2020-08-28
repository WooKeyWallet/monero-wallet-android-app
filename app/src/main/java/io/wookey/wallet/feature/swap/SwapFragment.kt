package io.wookey.wallet.feature.swap

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseActivity
import io.wookey.wallet.base.BaseFragment
import io.wookey.wallet.core.XMRWalletController
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.dialog.ExtraIDPromptDialog
import io.wookey.wallet.dialog.PickerDialog
import io.wookey.wallet.dialog.SwapRiskDialog
import io.wookey.wallet.feature.address.ScanActivity
import io.wookey.wallet.feature.auth.AuthManager
import io.wookey.wallet.support.*
import io.wookey.wallet.support.extensions.*
import io.wookey.wallet.widget.IOSDialog
import io.wookey.wallet.widget.MaterialProgressDrawable
import kotlinx.android.synthetic.main.base_title_second.*
import kotlinx.android.synthetic.main.fragment_swap.*

class SwapFragment : BaseFragment() {

    private var currentToCurrency = ""
    private var walletId = -1
    val viewModel by viewModel<SwapViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_swap, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val appCompatActivity = activity as AppCompatActivity?
        toolbar.setBackgroundColor(ContextCompat.getColor(context!!, R.color.color_FFFFFF))
        appCompatActivity?.setSupportActionBar(toolbar)
        appCompatActivity?.supportActionBar?.apply {
            setDisplayShowHomeEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
        }

        centerTitle.setText(R.string.swap)

        with(rightIcon) {
            visibility = View.VISIBLE
            setImageResource(R.drawable.icon_swap_records)
            setOnClickListener {
                startActivity(Intent(context, SwapRecordsActivity::class.java))
            }
        }

        toCoin.setOnClickListener { _ ->
            viewModel.currencies.value?.let { list ->
                PickerDialog.display(childFragmentManager, list, currentToCurrency, null) {
                    if (it != currentToCurrency) {
                        currentToCurrency = it
                        fromAmount.setText("")
                        fromAmount.hint = ""
                        toAddress.editText?.setText("")
                        toAddressTag.editText?.setText("")
                        extraID.editText?.setText("")
                        rate.text = ""
                        toAmount.text = ""
                        viewModel.switchToCoin(currentToCurrency)
                    }
                }
            }
        }

        scan.setOnClickListener {
            scanAddress()
        }

        addressBook.setOnClickListener {
            selectAddress(currentToCurrency)
        }

        saveTag.isChecked = false
        saveTag.setOnCheckedChangeListener { buttonView, isChecked ->
            toAddressTag.visibility = if (isChecked) View.VISIBLE else View.GONE
            viewModel.setEnabledAddressTag(isChecked)
        }

        extraIDHelp.setOnClickListener {
            ExtraIDPromptDialog.display(childFragmentManager)
        }

        more.isChecked = false
        more.setOnCheckedChangeListener { buttonView, isChecked ->
            more.setText(if (isChecked) R.string.fold_option else R.string.expend_option)
            moreGroup.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        more.background = BackgroundHelper.getBackground(context, R.color.color_B2F5F6F7, dp2px(5))

        viewModel.enabled.observe(this, Observer {
            if (it == true) {
                swapBg.setBackgroundResource(R.drawable.button_enabled_bg)
            } else {
                swapBg.setBackgroundResource(R.drawable.button_disabled_bg)
            }
        })

        swapBg.setOnClickListener {
            if (viewModel.enabled.value != true) {
                return@setOnClickListener
            }
            val agreeSwap = sharedPreferences().getBoolean("agreeSwap", false)
            if (agreeSwap) {
                viewModel.getWalletRelease(walletId)
            } else {
                SwapRiskDialog.display(childFragmentManager, null) {
                    sharedPreferences().putBoolean("agreeSwap", true)
                    viewModel.getWalletRelease(walletId)
                }
            }
        }

        viewModel.walletRelease.observe(this, Observer {
            if (walletId < 0) {
                toast("data exception")
            } else {
                AuthManager(it, walletId).sendTransaction(
                    activity as BaseActivity,
                    this
                ) { password ->
                    password?.let { pwd ->
                        viewModel.confirm(pwd, walletId)
                    }
                }
            }
        })

        fromAmount.filters = arrayOf(PointLengthFilter(8))
        fromAmount.afterDecimalTextChanged { viewModel.setFromAmount(it) }
        toAddress.editText?.afterTextChanged { viewModel.setToAddress(it) }
        toAddressTag.editText?.afterTextChanged { viewModel.setToAddressTag(it) }
        extraID.editText?.afterTextChanged { viewModel.setExtraID(it) }
        refundAddress.editText?.afterTextChanged { viewModel.setRefundAddress(it) }

        viewModel.loadData()

        viewModel.toCurrency.observe(this, Observer {
            currentToCurrency = it ?: ""
            toCoin.text = it?.displayCoin() ?: ""
            fromAmount.setText("")
            toAddress.editText?.setText("")
            toAddressTag.editText?.setText("")
            extraID.editText?.setText("")
            if ("EOS".equals(it, true)) {
                more.isChecked = true
                extraID.hint = getString(R.string.swap_to_extra_id_required)
            } else {
                extraID.hint = getString(R.string.swap_to_extra_id_optional)
            }
        })

        viewModel.minFromAmount.observe(this, Observer {
            if (it.isNullOrBlank()) {
                fromAmount.hint = ""
            } else {
                fromAmount.hint = getString(R.string.swap_from_amount_hint, it)
            }
            viewModel.checkValid()
        })

        viewModel.exchangeAmount.observe(this, Observer {
            if (it != null) {
                if (it.rate.isNullOrBlank()) {
                    rate.text = "--"
                } else {
                    rate.text =
                        "1 ${it.from.toUpperCase()} ≈ ${it.rate.decimalFormat()} ${it.to.toUpperCase()}"
                }
            }
            viewModel.calculateExchangeAmount()
        })

        viewModel.balance.observe(this, Observer { value ->
            value?.let {
                clearFocus()
                balance.text = "${it.decimalFormat()} XMR"
            }
        })

        viewModel.estimatedAmount.observe(this, Observer {
            if (it.isNullOrBlank()) {
                toAmount.text = "0"
            } else {
                toAmount.text = it.decimalFormat()
            }
        })

        viewModel.showLoading.observe(this, Observer { showLoading() })
        viewModel.hideLoading.observe(this, Observer { hideLoading() })
        viewModel.toast.observe(this, Observer { toast(it) })
        viewModel.toastRes.observe(this, Observer { toast(it) })

        AppDatabase.getInstance().walletDao().loadActiveWallet().observe(this, Observer { wallet ->
            wallet?.let {
                walletId = it.id
                balance.text = "${it.balance.decimalFormat()} XMR"
                refundAddress.editText?.setText(it.address)
            }
        })

        val animatedDrawable = MaterialProgressDrawable(context, swap)
        val radius = 10.0
        animatedDrawable.setSizeParameters(2 * radius, 2 * radius, radius, 2.0, 10f, 5f)

        viewModel.connecting.observe(this, Observer { value ->
            value?.let {
                swap.setText(it)
                swap.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                animatedDrawable.stop()
                animatedDrawable.start()
                swap.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    animatedDrawable,
                    null,
                    null,
                    null
                )
            }
        })

        viewModel.synchronizing.observe(this, Observer { value ->
            value?.let {
                swap.text = getString(R.string.block_synchronizing, it.toString())
                swap.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                animatedDrawable.stop()
                animatedDrawable.start()
                swap.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    animatedDrawable,
                    null,
                    null,
                    null
                )
            }
        })

        viewModel.synchronized.observe(this, Observer { value ->
            value?.let {
                swap.setText(it)
                swap.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                animatedDrawable.stop()
                animatedDrawable.start()
                swap.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    animatedDrawable,
                    null,
                    null,
                    null
                )
            }
        })

        viewModel.sendStatus.observe(this, Observer { value ->
            clearFocus()
            when (value) {
                -1 -> {
                    intercept.isIntercept = false
                    swap.setText(R.string.swap)
                    swap.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                    animatedDrawable.stop()
                    swap.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null,
                        null,
                        null,
                        null
                    )
                }
                1 -> {
                    intercept.isIntercept = false
                    toast(R.string.swap_send_transaction)
                    fromAmount.setText("")
                    toAddress.editText?.setText("")
                    toAddressTag.editText?.setText("")
                    extraID.editText?.setText("")
                    swap.setText(R.string.swap)
                    swap.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                    animatedDrawable.stop()
                    swap.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null,
                        null,
                        null,
                        null
                    )
                }
                0 -> {
                    intercept.isIntercept = true
                    animatedDrawable.stop()
                    animatedDrawable.start()
                    swap.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        animatedDrawable,
                        null,
                        null,
                        null
                    )
                }
            }
        })

        XMRWalletController.stopWallet.observe(this, Observer {
            if (it != null) {
                if (viewModel.walletResume.get()) {
                    viewModel.failed(R.string.swap_transaction_interrupt)
                }
            }
        })
    }

    private fun clearFocus() {
        fromAmount.clearFocus()
        toAddress.clearFocus()
        toAddressTag.clearFocus()
        extraID.clearFocus()
        refundAddress.clearFocus()
    }

    private fun scanAddress() {
        startActivityForResult(
            Intent(activity!!, ScanActivity::class.java),
            REQUEST_SWAP_SCAN_ADDRESS
        )
    }

    private fun selectAddress(symbol: String) {
        startActivityForResult(Intent(activity!!, SwapAddressBookActivity::class.java).apply {
            putExtra("symbol", symbol)
        }, REQUEST_SWAP_SELECT_ADDRESS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_SWAP_SCAN_ADDRESS -> {
                data?.getStringExtra("result")?.let {
                    if (it.isNotBlank()) {
                        toAddress.editText?.setText(it)
                    }
                }
            }
            REQUEST_SWAP_SELECT_ADDRESS -> {
                data?.getStringExtra("result")?.let {
                    if (it.isNotBlank()) {
                        toAddress.editText?.setText(it)
                    }
                }
                data?.getStringExtra("tag")?.let {
                    if (it.isNotBlank()) {
                        saveTag.isChecked = true
                        toAddressTag.editText?.setText(it)
                    }
                }
            }
            REQUEST_PATTERN_CHECKING -> {
                data?.getStringExtra("password")?.let {
                    viewModel.confirm(it, walletId)
                }
            }
        }
    }

    fun onSwitchFragment(currentPosition: Int, position: Int, callback: () -> Unit) {
        if (isResumed) {
            if (currentPosition == position) {
                if (nsv.scrollY == 0) {
                    viewModel.loadData()
                } else {
                    nsv.smoothScrollTo(0, 0)
                }
            } else {
                if (viewModel.send) {
                    callback.invoke()
                } else {
                    showConfirm(callback)
                }
            }
        }
    }

    private fun showConfirm(callback: () -> Unit) {
        IOSDialog(activity!!)
            .radius(dp2px(5))
            .titleText("")
            .contentText(getString(R.string.dialog_left_current_screen))
            .contentTextSize(16)
            .contentTextBold(true)
            .leftText(getString(R.string.cancel))
            .rightText(getString(R.string.confirm))
            .setIOSDialogRightListener { callback.invoke() }
            .cancelAble(true)
            .layout()
            .show()
    }
}
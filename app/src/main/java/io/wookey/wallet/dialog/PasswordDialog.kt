package io.wookey.wallet.dialog

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.*
import io.wookey.wallet.R
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.afterTextChanged
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.hideKeyboard
import io.wookey.wallet.support.extensions.screenWidth
import kotlinx.android.synthetic.main.dialog_password.*

class PasswordDialog : DialogFragment() {

    private var cancelListener: (() -> Unit)? = null
    private var confirmListener: ((String) -> Unit)? = null
    private var supportCancel = true
    private var walletId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_password, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewModel = ViewModelProviders.of(this).get(PasswordViewModel::class.java)

        val layoutParams = editContainer.layoutParams
        layoutParams.width = (screenWidth() * 0.85).toInt()
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        editContainer.background = BackgroundHelper.getBackground(context, R.color.color_FFFFFF, dp2px(5))
        password.background = BackgroundHelper.getEditBackground(context)
        password.afterTextChanged {
            error.visibility = View.INVISIBLE
        }

        confirm.background = BackgroundHelper.getButtonBackground(context)

        confirm.setOnClickListener {
            viewModel.verify(password.text.toString().trim(), walletId)
        }

        if (supportCancel) {
            cancel.visibility = View.VISIBLE
        } else {
            cancel.visibility = View.GONE
        }

        cancel.setOnClickListener {
            cancelListener?.invoke()
            hide()
        }

        viewModel.verifyPassed.observe(this, Observer { value ->
            value?.let {
                error.visibility = View.INVISIBLE
                confirmListener?.invoke(it)
                hide()
            }
        })
        viewModel.verifyFailed.observe(this, Observer {
            error.visibility = View.VISIBLE
        })
    }

    private fun hide() {
        val activity = activity
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            password?.hideKeyboard()
            dismiss()
        }
    }

    companion object {
        private const val TAG = "PasswordDialog"
        fun newInstance(): PasswordDialog {
            val fragment = PasswordDialog()
            return fragment
        }

        fun display(fm: FragmentManager, walletId: Int, supportCancel: Boolean = true, cancelListener: (() -> Unit)? = null, confirmListener: ((String) -> Unit)?) {
            if (walletId < 0) {
                return
            }
            val ft = fm.beginTransaction()
            val prev = fm.findFragmentByTag(TAG)
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)
            newInstance().apply {
                this.supportCancel = supportCancel
                this.walletId = walletId
                this.cancelListener = cancelListener
                this.confirmListener = confirmListener
            }.show(ft, TAG)
        }
    }
}
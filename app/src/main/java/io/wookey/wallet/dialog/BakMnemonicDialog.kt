package io.wookey.wallet.dialog

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import io.wookey.wallet.R
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.screenWidth
import kotlinx.android.synthetic.main.dialog_bak_mnemonic.*

class BakMnemonicDialog : DialogFragment() {

    private var cancelListener: (() -> Unit)? = null
    private var confirmListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_bak_mnemonic, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val layoutParams = container.layoutParams
        layoutParams.width = (screenWidth() * 0.85).toInt()
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        container.background = BackgroundHelper.getBackground(context, R.color.color_FFFFFF, dp2px(5))

        confirm.background = BackgroundHelper.getButtonBackground(context)

        confirm.setOnClickListener {
            confirmListener?.invoke()
            dismiss()
        }

    }

    companion object {
        private const val TAG = "BakMnemonicDialog"
        fun newInstance(): BakMnemonicDialog {
            val fragment = BakMnemonicDialog()
            return fragment
        }

        fun display(fm: FragmentManager, cancelListener: (() -> Unit)? = null, confirmListener: (() -> Unit)?) {
            val ft = fm.beginTransaction()
            val prev = fm.findFragmentByTag(TAG)
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)
            newInstance().apply {
                this.cancelListener = cancelListener
                this.confirmListener = confirmListener
            }.show(ft, TAG)
        }
    }
}
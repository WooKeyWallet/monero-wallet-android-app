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
import kotlinx.android.synthetic.main.dialog_password_prompt.*

class PasswordPromptDialog : DialogFragment() {

    private var prompt = ""
    private var confirmListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_password_prompt, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val layoutParams = editContainer.layoutParams
        layoutParams.width = (screenWidth() * 0.85).toInt()
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        editContainer.background = BackgroundHelper.getBackground(context, R.color.color_FFFFFF, dp2px(5))

        promptTv.text = if (prompt.isNullOrBlank()) getString(R.string.no_prompt) else prompt

        confirm.background = BackgroundHelper.getButtonBackground(context)

        confirm.setOnClickListener {
            confirmListener?.invoke()
            dismiss()
        }
    }

    companion object {
        private const val TAG = "PasswordPromptDialog"
        fun newInstance(): PasswordPromptDialog {
            val fragment = PasswordPromptDialog()
            return fragment
        }

        fun display(fm: FragmentManager, prompt: String, confirmListener: (() -> Unit)? = null) {
            val ft = fm.beginTransaction()
            val prev = fm.findFragmentByTag(TAG)
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)
            newInstance().apply {
                this.prompt = prompt
                this.confirmListener = confirmListener
            }.show(ft, TAG)
        }
    }
}
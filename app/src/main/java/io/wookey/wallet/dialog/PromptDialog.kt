package io.wookey.wallet.dialog

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import io.wookey.wallet.R
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.screenWidth
import kotlinx.android.synthetic.main.dialog_prompt.*

class PromptDialog : DialogFragment() {

    private var confirmListener: (() -> Unit)? = null
    private var titleStr = ""
    private var contentStr = ""
    private var confirmStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
        isCancelable = false
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_prompt, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val layoutParams = dialogContainer.layoutParams
        layoutParams.width = (screenWidth() * 0.85).toInt()
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        dialogContainer.background =
                BackgroundHelper.getBackground(context, R.color.color_FFFFFF, dp2px(5))
        confirm.background = BackgroundHelper.getButtonBackground(context)

        title.text = titleStr
        content.text = contentStr
        confirm.text = confirmStr

        confirm.setOnClickListener {
            confirmListener?.invoke()
            hide()
        }
    }

    fun hide() {
        val activity = activity
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            dismiss()
        }
    }

    companion object {
        private const val TAG = "PromptDialog"
        fun newInstance(): PromptDialog {
            val fragment = PromptDialog()
            return fragment
        }

        fun display(fm: androidx.fragment.app.FragmentManager, titleStr: String, contentStr: String, confirmStr: String, confirmListener: (() -> Unit)?) {
            val ft = fm.beginTransaction()
            val prev = fm.findFragmentByTag(TAG)
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)
            newInstance().apply {
                this.titleStr = titleStr
                this.contentStr = contentStr
                this.confirmStr = confirmStr
                this.confirmListener = confirmListener
            }.show(ft, TAG)
        }
    }
}
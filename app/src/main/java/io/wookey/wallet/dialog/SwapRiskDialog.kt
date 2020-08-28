package io.wookey.wallet.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import io.wookey.wallet.R
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.screenWidth
import kotlinx.android.synthetic.main.dialog_swap_risk.*

class SwapRiskDialog : DialogFragment() {

    private var cancelListener: (() -> Unit)? = null
    private var confirmListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        return inflater.inflate(R.layout.dialog_swap_risk, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog?.window?.let {
            val attributes = it.attributes.apply {
                gravity = Gravity.CENTER
                width = (0.85 * screenWidth()).toInt()
            }
            it.attributes = attributes
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.decorView.setPadding(0, 0, 0, 0)
            it.setLayout(attributes.width, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        editContainer.background =
            BackgroundHelper.getBackground(context, R.color.color_FFFFFF, dp2px(5))

        close.setOnClickListener {
            cancelListener?.invoke()
            hide()
        }

        risk.movementMethod = ScrollingMovementMethod.getInstance()

        agree.setOnCheckedChangeListener { buttonView, isChecked ->
            confirm.isEnabled = isChecked
        }

        confirm.background = BackgroundHelper.getButtonBackground(context)
        confirm.setOnClickListener {
            confirmListener?.invoke()
            hide()
        }
    }

    fun hide() {
        activity?.let {
            if (!it.isFinishing && !it.isDestroyed) {
                dismiss()
            }
        }
    }

    companion object {
        private const val TAG = "SwapRiskDialog"
        fun newInstance(): SwapRiskDialog {
            return SwapRiskDialog()
        }

        fun display(
            fm: FragmentManager,
            cancelListener: (() -> Unit)? = null,
            confirmListener: (() -> Unit)? = null
        ) {
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
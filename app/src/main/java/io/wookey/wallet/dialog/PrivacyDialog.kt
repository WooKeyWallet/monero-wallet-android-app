package io.wookey.wallet.dialog

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import io.wookey.wallet.R
import io.wookey.wallet.feature.setting.WebViewActivity
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.clickableSpan
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.getCurrentLocale
import io.wookey.wallet.support.extensions.screenWidth
import kotlinx.android.synthetic.main.dialog_privacy.*

class PrivacyDialog : DialogFragment() {

    private var confirmListener: (() -> Unit)? = null
    private var cancelListener: (() -> Unit)? = null

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
        return inflater.inflate(R.layout.dialog_privacy, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val layoutParams = dialogContainer.layoutParams
        layoutParams.width = (screenWidth() * 0.85).toInt()
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        dialogContainer.background =
            BackgroundHelper.getBackground(context, R.color.color_FFFFFF, dp2px(5))
        confirm.background = BackgroundHelper.getButtonBackground(context)

        val agreement = getString(R.string.user_agreement)
        val privacy = getString(R.string.privacy)
        val contentText = getString(R.string.dialog_content_privacy, agreement, privacy)

        val start1 = contentText.indexOf(agreement)
        val end1 = start1 + agreement.length
        val start2 = contentText.indexOf(privacy)
        val end2 = start2 + privacy.length

        val style = SpannableString(contentText)
        style.clickableSpan(start1..end1, ContextCompat.getColor(activity!!, R.color.color_2179FF)) {
            startActivity(Intent(activity, WebViewActivity::class.java).apply {
                putExtra("url", "https://wallet.wookey.io/service-docs/terms-of-service.html")
            })
        }
        style.clickableSpan(start2..end2, ContextCompat.getColor(activity!!, R.color.color_2179FF)) {
            startActivity(Intent(activity, WebViewActivity::class.java).apply {
                putExtra("url", "https://wallet.wookey.io/service-docs/privacy-policy.html?lang=${activity?.getCurrentLocale()}")
            })
        }

        content.text = style
        content.movementMethod = LinkMovementMethod.getInstance()

        confirm.setOnClickListener {
            confirmListener?.invoke()
            hide()
        }

        cancel.setOnClickListener {
            hide()
            cancelListener?.invoke()
        }
    }

    fun hide() {
        val activity = activity
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            dismiss()
        }
    }

    companion object {
        private const val TAG = "PrivacyDialog"
        fun newInstance(): PrivacyDialog {
            val fragment = PrivacyDialog()
            return fragment
        }

        fun display(
            fm: androidx.fragment.app.FragmentManager,
            confirmListener: (() -> Unit)?,
            cancelListener: (() -> Unit)?
        ) {
            val ft = fm.beginTransaction()
            val prev = fm.findFragmentByTag(TAG)
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)
            newInstance().apply {
                this.confirmListener = confirmListener
                this.cancelListener = cancelListener
            }.show(ft, TAG)
        }
    }
}
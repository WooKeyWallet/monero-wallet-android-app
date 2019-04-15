package io.wookey.wallet.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import io.wookey.wallet.R
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.utils.ShadowDrawable
import kotlinx.android.synthetic.main.loading_dialog.*

class LoadingDialog(context: Context) : Dialog(context, R.style.LoadingDialogTheme) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.loading_dialog)
        ShadowDrawable.setShadowDrawable(progressBar, Color.parseColor("#F2FFFFFF"),
                dp2px(10),
                Color.parseColor("#4C000000"),
                dp2px(16),
                0, 0)
        try {
            val params = window!!.attributes
            params.width = WindowManager.LayoutParams.WRAP_CONTENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window!!.attributes = params
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
package io.wookey.wallet.base

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import io.wookey.wallet.dialog.LoadingDialog
import io.wookey.wallet.support.extensions.getLocale
import io.wookey.wallet.support.extensions.setLocale

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(setLocale(newBase, getLocale()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 禁止截图
        if (hide()) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    open fun hide(): Boolean = true

    var loadingDialog: LoadingDialog? = null

    open fun showLoading() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(this)
        }
        loadingDialog?.show()
    }

    open fun hideLoading() {
        if (!isFinishing && !isDestroyed) loadingDialog?.dismiss()
    }
}
package io.wookey.wallet.dialog

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import io.wookey.wallet.R
import io.wookey.wallet.data.entity.Node
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.hideKeyboard
import io.wookey.wallet.support.extensions.screenWidth
import io.wookey.wallet.support.extensions.toast
import kotlinx.android.synthetic.main.dialog_node_edit.*

class NodeEditDialog : DialogFragment() {

    private var cancelListener: (() -> Unit)? = null
    private var confirmListener: ((Node?) -> Unit)? = null
    private var symbol = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_node_edit, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewModel = ViewModelProviders.of(this).get(NodeEditViewModel::class.java)

        val layoutParams = editContainer.layoutParams
        layoutParams.width = (screenWidth() * 0.85).toInt()
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        editContainer.background = BackgroundHelper.getBackground(context, R.color.color_FFFFFF, dp2px(5))
        current.text = getString(R.string.current_node_prompt, symbol)
        nodeUrl.background = BackgroundHelper.getEditBackground(context)

        confirm.background = BackgroundHelper.getButtonBackground(context)

        confirm.setOnClickListener {
            val url = nodeUrl.text.toString().trim()
            if (url.isNullOrBlank()) {
                toast(R.string.edit_node_url_hint)
            } else {
                viewModel.testRpcService(symbol, url)
            }
        }

        cancel.setOnClickListener {
            cancelListener?.invoke()
            hide()
        }

        viewModel.showLoading.observe(this, Observer {
            confirm.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        })

        viewModel.hideLoading.observe(this, Observer {
            confirm.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        })

        viewModel.toastRes.observe(this, Observer { toast(it) })

        viewModel.success.observe(this, Observer {
            confirmListener?.invoke(it)
            hide()
        })
    }

    fun hide() {
        val activity = activity
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            nodeUrl?.hideKeyboard()
            dismiss()
        }
    }

    companion object {
        private const val TAG = "NodeEditDialog"
        fun newInstance(): NodeEditDialog {
            val fragment = NodeEditDialog()
            return fragment
        }

        fun display(fm: FragmentManager, symbol: String = "", cancelListener: (() -> Unit)? = null, confirmListener: ((Node?) -> Unit)?) {
            val ft = fm.beginTransaction()
            val prev = fm.findFragmentByTag(TAG)
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)
            newInstance().apply {
                this.symbol = symbol
                this.cancelListener = cancelListener
                this.confirmListener = confirmListener
            }.show(ft, TAG)
        }
    }
}
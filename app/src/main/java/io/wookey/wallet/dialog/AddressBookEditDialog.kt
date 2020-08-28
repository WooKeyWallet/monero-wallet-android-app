package io.wookey.wallet.dialog

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import io.wookey.wallet.R
import io.wookey.wallet.data.entity.AddressBook
import io.wookey.wallet.data.entity.SwapAddressBook
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.hideKeyboard
import io.wookey.wallet.support.extensions.screenWidth
import io.wookey.wallet.support.extensions.toast
import kotlinx.android.synthetic.main.dialog_address_book_edit.*

class AddressBookEditDialog : DialogFragment() {

    private var cancelListener: (() -> Unit)? = null
    private var confirmListener: (() -> Unit)? = null
    private lateinit var addressBook: AddressBook
    private lateinit var swapAddressBook: SwapAddressBook

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
        return inflater.inflate(R.layout.dialog_address_book_edit, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewModel = ViewModelProviders.of(this).get(AddressBookEditViewModel::class.java)

        val layoutParams = editContainer.layoutParams
        layoutParams.width = (screenWidth() * 0.85).toInt()
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        editContainer.background =
            BackgroundHelper.getBackground(context, R.color.color_FFFFFF, dp2px(5))
        addressTag.background = BackgroundHelper.getEditBackground(context)

        confirm.background = BackgroundHelper.getButtonBackground(context)

        confirm.setOnClickListener { _ ->
            val tag = addressTag.text.toString().trim()
            if (tag.isNullOrBlank()) {
                toast(R.string.input_note)
            } else {
                if (::addressBook.isInitialized) {
                    addressBook.notes = tag
                    viewModel.updateAddressBook(addressBook)
                }
                if (::swapAddressBook.isInitialized) {
                    swapAddressBook.notes = tag
                    viewModel.updateAddressBook(swapAddressBook)
                }
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
            confirmListener?.invoke()
            hide()
        })
    }

    fun hide() {
        val activity = activity
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            addressTag?.hideKeyboard()
            dismiss()
        }
    }

    companion object {
        private const val TAG = "AddressBookEditDialog"
        fun newInstance(): AddressBookEditDialog {
            return AddressBookEditDialog()
        }

        fun display(
            fm: FragmentManager,
            addressBook: AddressBook,
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
                this.addressBook = addressBook
                this.cancelListener = cancelListener
                this.confirmListener = confirmListener
            }.show(ft, TAG)
        }

        fun display(
            fm: FragmentManager,
            swapAddressBook: SwapAddressBook,
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
                this.swapAddressBook = swapAddressBook
                this.cancelListener = cancelListener
                this.confirmListener = confirmListener
            }.show(ft, TAG)
        }
    }
}
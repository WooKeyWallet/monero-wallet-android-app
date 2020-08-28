package io.wookey.wallet.feature.address

import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.we.swipe.helper.WeSwipe
import cn.we.swipe.helper.WeSwipeHelper
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.AddressBook
import io.wookey.wallet.dialog.AddressBookEditDialog
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.setImage
import io.wookey.wallet.widget.DividerItemDecoration
import io.wookey.wallet.widget.StatusAdapterWrapper
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_address_book.*
import kotlinx.android.synthetic.main.item_common.*

class AddressBookActivity : BaseTitleSecondActivity() {

    private var isDelete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_book)
        setCenterTitle(R.string.address_book)
        setRightIcon(R.drawable.icon_add)
        setRightIconClick(View.OnClickListener {
            addAddress()
        })

        val viewModel = ViewModelProviders.of(this).get(AddressBookViewModel::class.java)

        val symbol = intent.getStringExtra("symbol")

        recyclerView.layoutManager = LinearLayoutManager(this)

        val list = mutableListOf<AddressBook>()
        val adapter = AddressAdapter(list, viewModel)

        val wrapper = object : StatusAdapterWrapper(adapter) {
            override fun getEmptyActionVisibility(): Int {
                return View.VISIBLE
            }

            override fun onEmptyClick() {
                addAddress()
            }
        }
        recyclerView.adapter = wrapper

        recyclerView.addItemDecoration(DividerItemDecoration().apply {
            setOrientation(DividerItemDecoration.VERTICAL)
            setMarginStart(dp2px(25))
        })

        //设置WeSwipe
        WeSwipe.attach(recyclerView)

        if (symbol.isNullOrBlank()) {
            AppDatabase.getInstance().addressBookDao().loadAddressBooks()
                .observe(this, Observer {
                    if (isDelete) {
                        return@Observer
                    }
                    list.clear()
                    if (!it.isNullOrEmpty()) {
                        list.addAll(it)
                    }
                    adapter.notifyDataSetChanged()
                    wrapper.setSuccessView()
                })
        } else {
            AppDatabase.getInstance().addressBookDao().loadAddressBooksBySymbol(symbol)
                .observe(this, Observer {
                    if (isDelete) {
                        return@Observer
                    }
                    list.clear()
                    if (!it.isNullOrEmpty()) {
                        list.addAll(it)
                    }
                    adapter.notifyDataSetChanged()
                    wrapper.setSuccessView()
                })
        }

        viewModel.itemClick.value = null
        viewModel.itemClick.observe(this, Observer { value ->
            value?.let {
                if (!symbol.isNullOrBlank()) {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra("result", it.address)
                    })
                    finish()
                }
            }
        })

        viewModel.isDelete.value = null
        viewModel.isDelete.observe(this, Observer { value ->
            value?.let {
                isDelete = it
            }
        })
        viewModel.deleteSuccess.observe(this, Observer { position ->
            if (position != null && position >= 0) {
                wrapper.notifyItemRemoved(position)
                wrapper.notifyItemRangeChanged(position, list.size - 1)
            }
            isDelete = false
        })

        viewModel.editAddressBook.value = null
        viewModel.editAddressBook.observe(this, Observer { value ->
            value?.let {
                AddressBookEditDialog.display(supportFragmentManager, it)
            }
        })
    }

    private fun addAddress() {
        startActivity(Intent(this, AddAddressActivity::class.java))
    }

    class AddressAdapter(val data: List<AddressBook>, val viewModel: AddressBookViewModel) :
        androidx.recyclerview.widget.RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_common, parent, false)
            return ViewHolder(view, viewModel)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindViewHolder(data[position], position)
        }

        class ViewHolder(override val containerView: View, val viewModel: AddressBookViewModel) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(containerView), LayoutContainer,
            WeSwipeHelper.SwipeLayoutTypeCallBack {

            private var needRecovery = false

            fun bindViewHolder(addressBook: AddressBook, position: Int) {
                with(addressBook) {
                    icon.setImage(symbol)
                    title.text = notes
                    subTitle.text = this.address
                    delete.setOnClickListener {
                        needRecovery =
                            if (delete.text == delete.context.getString(R.string.delete)) {
                                delete.setText(R.string.delete_confirm)
                                false
                            } else {
                                viewModel.deleteAddressBook(addressBook, position)
                                true
                            }
                    }
                    edit.setOnClickListener {
                        viewModel.edit(addressBook)
                    }
                    item.setOnClickListener {
                        viewModel.itemClick(addressBook)
                    }
                }
            }

            override fun getSwipeWidth() = delete.width.toFloat()

            override fun onScreenView(): View = item

            override fun needSwipeLayout(): View = item

            override fun needRecoveryOpened(): Boolean {

                return needRecovery
            }

            override fun recoveryOpened() {
                if (!needRecovery) {
                    delete.setText(R.string.delete)
                }
            }
        }
    }
}

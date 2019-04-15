package io.wookey.wallet.feature.address

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.AddressBook
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.setImage
import io.wookey.wallet.widget.DividerItemDecoration
import io.wookey.wallet.widget.StatusAdapterWrapper
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_address_book.*
import kotlinx.android.synthetic.main.item_common.*

class AddressBookActivity : BaseTitleSecondActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_book)
        setCenterTitle(R.string.address_book)
        setRightIcon(R.drawable.icon_add)
        setRightIconClick(View.OnClickListener {
            addAddress()
        })

        val symbol = intent.getStringExtra("symbol")

        recyclerView.layoutManager = LinearLayoutManager(this)

        val list = mutableListOf<AddressBook>()
        val adapter = AddressAdapter(list) {
            if (!symbol.isNullOrBlank()) {
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra("result", it.address)
                })
                finish()
            }
        }
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

        if (symbol.isNullOrBlank()) {
            AppDatabase.getInstance().addressBookDao().loadAddressBooks().observe(this, Observer {
                list.clear()
                if (!it.isNullOrEmpty()) {
                    list.addAll(it)
                }
                adapter.notifyDataSetChanged()
                wrapper.setSuccessView()
            })
        } else {
            AppDatabase.getInstance().addressBookDao().loadAddressBooksBySymbol(symbol).observe(this, Observer {
                list.clear()
                if (!it.isNullOrEmpty()) {
                    list.addAll(it)
                }
                adapter.notifyDataSetChanged()
                wrapper.setSuccessView()
            })
        }
    }

    private fun addAddress() {
        startActivity(Intent(this, AddAddressActivity::class.java))
    }

    class AddressAdapter(val data: List<AddressBook>, private val listener: (AddressBook) -> Unit) : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_common, parent, false)
            return ViewHolder(view, listener)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindViewHolder(data[position])
        }

        class ViewHolder(override val containerView: View, private val listener: (AddressBook) -> Unit) : RecyclerView.ViewHolder(containerView), LayoutContainer {

            fun bindViewHolder(addressBook: AddressBook) {
                with(addressBook) {
                    icon.setImage(symbol)
                    title.text = notes
                    subTitle.text = this.address
                    itemView.setOnClickListener { listener(this) }
                }
            }
        }
    }
}

package io.wookey.wallet.feature.wallet

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.data.entity.SubAddress
import io.wookey.wallet.dialog.SubAddressEditDialog
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.copy
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.toast
import io.wookey.wallet.widget.DividerItemDecoration
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_address_setting.*
import kotlinx.android.synthetic.main.item_sub_address.*

class AddressSettingActivity : BaseTitleSecondActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_setting)

        val walletId = intent.getIntExtra("walletId", -1)
        val password = intent.getStringExtra("password")
        if (password.isNullOrBlank()) {
            finish()
            return
        }
        setCenterTitle(R.string.address_setting)
        setRightIcon(R.drawable.icon_add)
        dot1.setImageDrawable(BackgroundHelper.getDotDrawable(this, R.color.color_FFFFFF, dp2px(6)))
        dot2.setImageDrawable(BackgroundHelper.getDotDrawable(this, R.color.color_FFFFFF, dp2px(6)))

        val viewModel = ViewModelProviders.of(this).get(AddressSettingViewModel::class.java)
        viewModel.walletId = walletId
        viewModel.password = password
        viewModel.loadSubAddresses()

        setRightIconClick(View.OnClickListener {
            viewModel.updateAddress.value = null
            SubAddressEditDialog.display(supportFragmentManager) {
                viewModel.refreshSubAddresses()
            }
        })

        viewModel.updateAddress.observe(this, Observer { value ->
            value?.let {
                SubAddressEditDialog.display(supportFragmentManager, viewModel.walletId, it) {
                    viewModel.refreshSubAddresses()
                }
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        val list = mutableListOf<SubAddress>()
        val adapter = SubAddressAdapter(list, viewModel)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration().apply {
            setOrientation(DividerItemDecoration.VERTICAL)
            setMarginStart(dp2px(25))
        })

        viewModel.subAddresses.observe(this, Observer {
            list.clear()
            if (it != null) {
                list.addAll(it)
            }
            adapter.notifyDataSetChanged()
        })

        viewModel.copy.observe(this, Observer {
            copy(it)
        })

        viewModel.dataChanged.observe(this, Observer {
            adapter.notifyDataSetChanged()
        })

        viewModel.showLoading.observe(this, Observer { showLoading() })
        viewModel.hideLoading.observe(this, Observer { hideLoading() })

        viewModel.toast.observe(this, Observer { toast(it) })
        viewModel.toastRes.observe(this, Observer { toast(it) })

    }

    class SubAddressAdapter(val data: List<SubAddress>, val viewModel: AddressSettingViewModel) :
        RecyclerView.Adapter<SubAddressAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sub_address, parent, false)
            return ViewHolder(view, viewModel)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindViewHolder(data[position])
        }

        class ViewHolder(override val containerView: View, val viewModel: AddressSettingViewModel) :
            RecyclerView.ViewHolder(containerView), LayoutContainer {
            fun bindViewHolder(subAddress: SubAddress) {
                address.text = subAddress.address
                if (subAddress.label.isNullOrBlank()) {
                    label.text = label.context.getString(R.string.no_label)
                } else {
                    label.text = subAddress.label
                }
                if (subAddress.address == viewModel.currentAddress) {
                    select.setImageResource(R.drawable.icon_selected)
                } else {
                    select.setImageResource(R.drawable.icon_unselected)
                }
                label.setOnClickListener {
                    viewModel.onLabelClick(subAddress)
                }
                address.setOnClickListener {
                    viewModel.onAddressClick(subAddress)
                }
                itemView.setOnClickListener {
                    viewModel.onItemClick(subAddress)
                }
            }
        }

    }
}
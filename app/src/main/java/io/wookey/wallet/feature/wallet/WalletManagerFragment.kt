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
import io.wookey.wallet.base.BaseFragment
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.Wallet
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.formatterAmountStrip
import io.wookey.wallet.support.extensions.setImage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_wallet_manager.*
import kotlinx.android.synthetic.main.item_wallet.*

class WalletManagerFragment : BaseFragment() {

    private var symbol: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wallet_manager, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewModel = ViewModelProviders.of(this).get(WalletManagerViewModel::class.java)

        recyclerView.layoutManager = LinearLayoutManager(context)
        val list = mutableListOf<Wallet>()
        val adapter = WalletAdapter(list, viewModel)
        recyclerView.adapter = adapter

        if (!symbol.isNullOrBlank()) {
            AppDatabase.getInstance().walletDao().loadSymbolWallets(symbol!!).observe(this, Observer { value ->
                value?.let {
                    list.clear()
                    list.addAll(it)
                    adapter.notifyDataSetChanged()
                }
            })
        }
        viewModel.activeWallet.observe(this, Observer {
            activity?.finish()
        })

        viewModel.walletDetail.observe(this, Observer { value ->
            value?.let {
                startActivity(it.apply {
                    setClass(context!!, WalletDetailActivity::class.java)
                })
            }
        })
    }

    companion object {
        fun newInstance(symbol: String): WalletManagerFragment {
            return WalletManagerFragment().apply {
                this.symbol = symbol
            }
        }
    }

    class WalletAdapter(val data: List<Wallet>, private val viewModel: WalletManagerViewModel) : RecyclerView.Adapter<WalletAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallet, parent, false)
            return ViewHolder(view, viewModel)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindViewHolder(data[position])
        }

        class ViewHolder(override val containerView: View, val viewModel: WalletManagerViewModel) : RecyclerView.ViewHolder(containerView), LayoutContainer {
            fun bindViewHolder(wallet: Wallet) {
                name.text = wallet.name
                if (wallet.isActive) {
                    current.visibility = View.VISIBLE
                    current.background = BackgroundHelper.getBackground(current.context, R.color.color_59C698, dp2px(3))
                    active.visibility = View.GONE
                } else {
                    current.visibility = View.GONE
                    active.visibility = View.VISIBLE
                }
                active.setOnClickListener {
                    viewModel.activeWallet(wallet)
                }
                icon.setImage(wallet.symbol)
                symbol.text = wallet.symbol
                balance.text = wallet.balance.formatterAmountStrip()
                BackgroundHelper.setItemShadowBackground(itemView)
                itemView.setOnClickListener { viewModel.onItemClick(wallet) }
            }
        }
    }
}
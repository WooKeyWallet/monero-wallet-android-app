package io.wookey.wallet.feature.asset

import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseActivity
import io.wookey.wallet.base.BaseFragment
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.Asset
import io.wookey.wallet.feature.wallet.WalletManagerActivity
import io.wookey.wallet.feature.auth.AuthManager
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.REQUEST_PATTERN_CHECKING
import io.wookey.wallet.support.extensions.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.base_title_second.*
import kotlinx.android.synthetic.main.fragment_asset.*
import kotlinx.android.synthetic.main.item_asset.*
import kotlinx.android.synthetic.main.item_asset.title

class AssetFragment : BaseFragment() {

    private lateinit var viewModel: AssetViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_asset, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(AssetViewModel::class.java)

        val appCompatActivity = activity as AppCompatActivity?
        appCompatActivity?.setSupportActionBar(toolbar)
        appCompatActivity?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
        }
        with(leftIcon) {
            visibility = View.VISIBLE
            setImageResource(R.drawable.icon_home)
            setOnClickListener {
                startActivity(Intent(context, WalletManagerActivity::class.java))
            }
        }

        with(rightIcon) {
            visibility = View.VISIBLE
            setImageResource(R.drawable.icon_visible)
            setOnClickListener {
                viewModel.assetVisibleChanged()
            }
        }

        walletBg.setRateHeight(686 / 210f, screenWidth() - dp2px(16 * 2))
        dot.setImageDrawable(BackgroundHelper.getDotDrawable(context, R.color.color_59C698, dp2px(9)))

        copy.background = BackgroundHelper.getStokeBackground(context, R.color.color_FFFFFF, 1, dp2px(11))
        copy.setOnClickListener { copy(address.text.toString()) }

        BackgroundHelper.setItemShadowBackground(assetBg)

        recyclerView.layoutManager = LinearLayoutManager(context)

        val list = mutableListOf<Asset>()
        val adapter = AssetAdapter(list) {
            viewModel.onItemClick(it)
        }
        recyclerView.adapter = adapter

        viewModel.openAssetDetail.observe(this, Observer { value ->
            value?.let {
                startActivity(it.apply {
                    setClass(context!!, AssetDetailActivity::class.java)
                })
            }
        })

        viewModel.assetVisible.observe(this, Observer {
            rightIcon.setImageResource(R.drawable.icon_visible)
            adapter.notifyVisibilityChanged(true)
        })

        viewModel.assetInvisible.observe(this, Observer {
            rightIcon.setImageResource(R.drawable.icon_invisible)
            adapter.notifyVisibilityChanged(false)
        })

        viewModel.initVisible()

        AppDatabase.getInstance().walletDao().loadActiveWallet().observe(this, Observer { wallet ->
            wallet?.let {
                centerTitle.text = it.symbol
                walletName.text = it.name
                address.text = it.address
                viewModel.wallet = it

                AppDatabase.getInstance().assetDao().loadAssetsByWalletId(it.id).observe(this, Observer { value ->
                    value?.let { assets ->
                        list.clear()
                        list.addAll(assets)
                        adapter.notifyDataSetChanged()
                    }
                })
            }
        })

        viewModel.walletRelease.observe(this, Observer {
            val id = viewModel.wallet?.id ?: return@Observer
            AuthManager(it, id).openWallet(activity as BaseActivity, this) { password ->
                password?.let { pwd ->
                    viewModel.next(pwd)
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_PATTERN_CHECKING -> {
                data?.getStringExtra("password")?.let {
                    viewModel.next(it)
                }
            }
        }
    }

    class AssetAdapter(val data: List<Asset>, private val listener: (Asset) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<AssetAdapter.ViewHolder>() {

        private var visibility = true

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_asset, parent, false)
            return ViewHolder(view, listener)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindViewHolder(data[position], visibility)
        }

        fun notifyVisibilityChanged(value: Boolean) {
            visibility = value
            notifyDataSetChanged()
        }

        class ViewHolder(override val containerView: View, private val listener: (Asset) -> Unit) : androidx.recyclerview.widget.RecyclerView.ViewHolder(containerView), LayoutContainer {

            fun bindViewHolder(asset: Asset, visibility: Boolean) {
                with(asset) {
                    icon.setImage(token)
                    title.text = token
                    if (visibility) {
                        subTitle.text = balance.formatterAmountStrip()
                    } else {
                        subTitle.text = "******"
                    }
                    itemView.setOnClickListener {
                        listener(this)
                    }
                }
            }
        }
    }
}
package io.wookey.wallet.feature.asset

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.view.View
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.dialog.PasswordDialog
import io.wookey.wallet.feature.setting.NodeListActivity
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.REQUEST_SELECT_NODE
import io.wookey.wallet.support.extensions.copy
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.formatterAmountStrip
import io.wookey.wallet.support.extensions.setImage
import kotlinx.android.synthetic.main.activity_asset_detail.*

class AssetDetailActivity : BaseTitleSecondActivity() {

    private lateinit var viewModel: AssetDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset_detail)

        viewModel = ViewModelProviders.of(this).get(AssetDetailViewModel::class.java)

        val password = intent.getStringExtra("password")
        val assetId = intent.getIntExtra("assetId", -1)

        if (password.isNullOrBlank()) {
            finish()
            return
        }

        if (assetId == -1) {
            finish()
            return
        }

        viewModel.setAssetId(assetId)

        setRightIcon(R.drawable.icon_switch_node)
        setRightIconClick(View.OnClickListener {
            val symbol = viewModel.activeWallet.value?.symbol ?: return@OnClickListener
            startActivityForResult(Intent(this, NodeListActivity::class.java).apply {
                putExtra("symbol", symbol)
                putExtra("canDelete", false)
            }, REQUEST_SELECT_NODE)
        })

        viewModel.loadWallet(password)

        viewModel.showPasswordDialog.observe(this, Observer {
            val id = viewModel.activeWallet.value?.id ?: return@Observer
            PasswordDialog.display(supportFragmentManager, id) { value ->
                viewModel.loadWallet(value)
            }
        })

        viewModel.refreshWallet.observe(this, Observer {
            viewModel.loadWallet(viewModel.password!!)
        })

        addressBg.background = BackgroundHelper.getEditBackground(this, dp2px(3))

        AppDatabase.getInstance().assetDao().loadAssetById(assetId).observe(this, Observer { value ->
            value?.let {
                setCenterTitle(it.token)
                icon.setImage(it.token)
                asset.text = it.balance.formatterAmountStrip()
                viewModel.activeAsset = it
            }
        })

        viewModel.activeWallet.observe(this, Observer { value ->
            value?.let {
                address.text = it.address
            }
        })

        AppDatabase.getInstance().walletDao().loadActiveWallet().observe(this, Observer { value ->
            value?.let {
                address.text = it.address
            }
        })

        addressBg.setOnClickListener { copy(address.text.toString()) }

        send.background = BackgroundHelper.getButtonBackground(this)
        receive.background = BackgroundHelper.getButtonBackground(this, R.color.color_AEB6C1, R.color.color_00A761)

        viewModel.sendEnabled.observe(this, Observer { value ->
            value?.let {
                send.isEnabled = it
            }
        })

        viewModel.receiveEnabled.observe(this, Observer { value ->
            value?.let {
                receive.isEnabled = it
            }
        })

        send.setOnClickListener {
            viewModel.send()
        }
        viewModel.openSend.observe(this, Observer {
            startActivity(Intent(this, SendActivity::class.java).apply {
                putExtra("assetId", assetId)
            })
        })

        receive.setOnClickListener {
            viewModel.receive()
        }
        viewModel.openReceive.observe(this, Observer {
            startActivity(Intent(this, ReceiveActivity::class.java).apply {
                putExtra("assetId", assetId)
                putExtra("password", password)
            })
        })

        val titles =
            arrayOf(getString(R.string.transfer_all), getString(R.string.receive), getString(R.string.send))
        val allTransfer = TransferFragment()
        val inTransfer = TransferFragment()
        val outTransfer = TransferFragment()
        val fragments = arrayOf(allTransfer, inTransfer, outTransfer)

        viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {

            override fun getItem(position: Int): Fragment {
                return fragments[position]
            }

            override fun getCount() = titles.size

            override fun getPageTitle(position: Int): CharSequence? {
                return titles[position]
            }
        }
        viewPager.offscreenPageLimit = 2
        tabLayout.setupWithViewPager(viewPager)

        viewModel.connecting.observe(this, Observer { value ->
            value?.let {
                state.text = getString(it)
                state.setTextColor(ContextCompat.getColor(this, R.color.color_9E9E9E))
            }
        })

        viewModel.synchronizing.observe(this, Observer { value ->
            value?.let {
                state.text = getString(R.string.block_synchronizing, it.toString())
                state.setTextColor(ContextCompat.getColor(this, R.color.color_9E9E9E))
            }
        })

        viewModel.synchronizeFailed.observe(this, Observer { value ->
            value?.let {
                state.text = getString(it)
                state.setTextColor(ContextCompat.getColor(this, R.color.color_FF3A5C))
                fragments.forEach { fragment ->
                    fragment.synchronizeFailed()
                }
            }
        })

        viewModel.synchronized.observe(this, Observer { value ->
            value?.let {
                state.text = getString(it)
                state.setTextColor(ContextCompat.getColor(this, R.color.color_9E9E9E))
            }
        })

        viewModel.synchronizeProgress.observe(this, Observer { value ->
            value?.let {
                progress.isIndeterminate = false
                val lp = progress.layoutParams
                lp.height = dp2px(4)
                progress.layoutParams = lp
                progress.progress = it
            }
        })

        viewModel.indeterminate.observe(this, Observer {
            progress.isIndeterminate = true
            val lp = progress.layoutParams
            lp.height = dp2px(16)
            progress.layoutParams = lp
        })

        viewModel.allTransfers.observe(this, Observer { value ->
            value?.let {
                allTransfer.notifyDataSetChanged(it)
            }
        })
        viewModel.inTransfers.observe(this, Observer { value ->
            value?.let {
                inTransfer.notifyDataSetChanged(it)
            }
        })
        viewModel.outTransfers.observe(this, Observer { value ->
            value?.let {
                outTransfer.notifyDataSetChanged(it)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.handleResult(requestCode, resultCode, data)
    }
}
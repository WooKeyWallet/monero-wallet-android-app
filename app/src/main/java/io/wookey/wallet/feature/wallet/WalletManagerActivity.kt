package io.wookey.wallet.feature.wallet

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.feature.coin.SelectCoinActivity
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.WALLET_CREATE
import io.wookey.wallet.support.WALLET_RECOVERY
import io.wookey.wallet.support.extensions.putInt
import io.wookey.wallet.support.extensions.sharedPreferences
import kotlinx.android.synthetic.main.activity_wallet_manager.*

class WalletManagerActivity : BaseTitleSecondActivity() {

    val titles = mutableListOf<String>()
    val fragments = mutableListOf<WalletManagerFragment>()
    val adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount() = titles.size

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_manager)
        setCenterTitle(R.string.wallet_manager)

        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 3
        tabLayout.setupWithViewPager(viewPager)

        recoveryWallet.background = BackgroundHelper.getButtonBackground(this, R.color.color_002C6D)
        createWallet.background = BackgroundHelper.getButtonBackground(this, R.color.color_00A761)

        recoveryWallet.setOnClickListener {
            startActivity(Intent(this, SelectCoinActivity::class.java).apply {
                sharedPreferences().putInt("type", WALLET_RECOVERY)
            })
        }

        createWallet.setOnClickListener {
            startActivity(Intent(this, SelectCoinActivity::class.java).apply {
                sharedPreferences().putInt("type", WALLET_CREATE)
            })
        }
    }

    override fun onResume() {
        super.onResume()
        AppDatabase.getInstance().walletDao().loadWalletSymbol().observe(this, Observer { value ->
            value?.let {
                titles.clear()
                fragments.clear()
                it.forEach { s ->
                    titles.add(s)
                    fragments.add(WalletManagerFragment.newInstance(s))
                }
                adapter.notifyDataSetChanged()
            }
        })
    }
}
package io.wookey.wallet.feature.generate.recovery

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import kotlinx.android.synthetic.main.activity_recovery_wallet.*

class RecoveryWalletActivity : BaseTitleSecondActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_recovery_wallet)
        setCenterTitle(R.string.recovery_wallet)

        val walletName = intent.getStringExtra("walletName")
        val password = intent.getStringExtra("password")
        val passwordPrompt = intent.getStringExtra("passwordPrompt")

        val titles = arrayOf(getString(R.string.recovery_mnemonic), getString(R.string.recovery_private_key))

        val mnemonicFragment = RecoveryMnemonicFragment().apply {
            arguments = Bundle().also {
                it.putString("walletName", walletName)
                it.putString("password", password)
                it.putString("passwordPrompt", passwordPrompt)
            }
        }
        val privateKeyFragment = RecoveryPrivateKeyFragment().apply {
            arguments = Bundle().also {
                it.putString("walletName", walletName)
                it.putString("password", password)
                it.putString("passwordPrompt", passwordPrompt)
            }
        }

        val fragments = arrayOf(mnemonicFragment, privateKeyFragment)

        viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return fragments[position]
            }

            override fun getCount(): Int = titles.size

            override fun getPageTitle(position: Int): CharSequence? {
                return titles[position]
            }

        }
        tabLayout.setupWithViewPager(viewPager)
    }
}

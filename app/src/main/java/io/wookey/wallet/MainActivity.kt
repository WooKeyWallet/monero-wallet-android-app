package io.wookey.wallet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import io.wookey.wallet.base.BaseActivity
import io.wookey.wallet.core.XMRWalletController
import io.wookey.wallet.feature.asset.AssetFragment
import io.wookey.wallet.feature.setting.LanguageActivity
import io.wookey.wallet.feature.setting.SettingFragment
import io.wookey.wallet.feature.swap.SwapFragment
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.utils.StatusBarHelper
import io.wookey.wallet.widget.IOSDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private val TAG_ASSET_FRAGMENT = "tag_asset_fragment"
    private val TAG_SWAP_FRAGMENT = "tag_swap_fragment"
    private val TAG_SETTING_FRAGMENT = "tag_setting_fragment"

    private var assetFragment: Fragment? = null
    private var swapFragment: Fragment? = null
    private var settingFragment: Fragment? = null

    private var currentPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        currentPosition = -1

        StatusBarHelper.setStatusBarLightMode(this)

        assetFragment = supportFragmentManager.findFragmentByTag(TAG_ASSET_FRAGMENT)
        swapFragment = supportFragmentManager.findFragmentByTag(TAG_SWAP_FRAGMENT)
        settingFragment = supportFragmentManager.findFragmentByTag(TAG_SETTING_FRAGMENT)

        assetIcon.setImageDrawable(
            BackgroundHelper.getSelectorDrawable(
                this,
                R.drawable.icon_asset_unselected,
                R.drawable.icon_asset_selected
            )
        )
        swapIcon.setImageDrawable(
            BackgroundHelper.getSelectorDrawable(
                this,
                R.drawable.icon_swap_unselected,
                R.drawable.icon_swap_selected
            )
        )
        settingIcon.setImageDrawable(
            BackgroundHelper.getSelectorDrawable(
                this,
                R.drawable.icon_setting_unselected,
                R.drawable.icon_setting_selected
            )
        )

        assetText.setTextColor(BackgroundHelper.getSelectorText(this))
        swapText.setTextColor(BackgroundHelper.getSelectorText(this))
        settingText.setTextColor(BackgroundHelper.getSelectorText(this))

        asset.setOnClickListener {
            switchFragment(0)
        }
        swap.setOnClickListener {
            switchFragment(1)
        }
        setting.setOnClickListener {
            switchFragment(2)
        }
        if (ActivityStackManager.getInstance().contain(LanguageActivity::class.java)) {
            switchFragment(2)
        } else {
            switchFragment(0)
        }
    }

    override fun onResume() {
        super.onResume()
        if (App.newVersion) {
            dot.visibility = View.VISIBLE
            dot.setImageDrawable(BackgroundHelper.getRedDotDrawable(this))
        } else {
            dot.visibility = View.GONE
        }
    }

    private fun switchFragment(position: Int) {
        if (currentPosition == 1) {
            (swapFragment as SwapFragment?)?.let {
                it.onSwitchFragment(currentPosition, position) {
                    realSwitch(position)
                }
            }
        } else {
            realSwitch(position)
        }
    }

    private fun realSwitch(position: Int) {
        if (currentPosition == position) {
            return
        }
        initSelected()
        val transaction = supportFragmentManager.beginTransaction()
        hideAllFragment(transaction)
        when (position) {
            0 -> {
                assetIcon.isSelected = true
                assetText.isSelected = true
                if (assetFragment == null) {
                    assetFragment = AssetFragment()
                    transaction.add(R.id.fragmentContainer, assetFragment!!, TAG_ASSET_FRAGMENT)
                } else {
                    transaction.show(assetFragment!!)
                }
            }
            1 -> {
                swapIcon.isSelected = true
                swapText.isSelected = true
                if (swapFragment == null) {
                    swapFragment = SwapFragment()
                    transaction.add(R.id.fragmentContainer, swapFragment!!, TAG_SWAP_FRAGMENT)
                } else {
                    transaction.show(swapFragment!!)
                }
            }
            2 -> {
                settingIcon.isSelected = true
                settingText.isSelected = true
                if (settingFragment == null) {
                    settingFragment = SettingFragment()
                    transaction.add(R.id.fragmentContainer, settingFragment!!, TAG_SETTING_FRAGMENT)
                } else {
                    transaction.show(settingFragment!!)
                }
            }
        }
        transaction.commitAllowingStateLoss()
        currentPosition = position
    }

    private fun initSelected() {
        assetIcon.isSelected = false
        assetText.isSelected = false
        swapIcon.isSelected = false
        swapText.isSelected = false
        settingIcon.isSelected = false
        settingText.isSelected = false
    }

    private fun hideAllFragment(transaction: androidx.fragment.app.FragmentTransaction) {
        assetFragment?.let {
            transaction.hide(it)
        }
        swapFragment?.let {
            transaction.hide(it)
        }
        settingFragment?.let {
            transaction.hide(it)
        }
    }
}
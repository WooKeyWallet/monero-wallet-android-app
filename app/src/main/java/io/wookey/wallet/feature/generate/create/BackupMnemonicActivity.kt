package io.wookey.wallet.feature.generate.create

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.dialog.BakMnemonicDialog
import io.wookey.wallet.dialog.PasswordDialog
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.copy
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.toast
import io.wookey.wallet.widget.SpaceItemDecoration
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_backup_mnemonic.*
import kotlinx.android.synthetic.main.item_mnemonic.*

class BackupMnemonicActivity : BaseTitleSecondActivity() {

    private var walletId = -1
    private var password: String? = null
    private var seed: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_backup_mnemonic)

        walletId = intent.getIntExtra("walletId", -1)
        password = intent.getStringExtra("password")
        seed = intent.getStringExtra("seed")
        if (walletId < 0 && password.isNullOrBlank() && seed.isNullOrBlank()) {
            finish()
            return
        }
        if (walletId >= 0 && !password.isNullOrBlank() && seed.isNullOrBlank()) {
            val actionBar = supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            val actionBar = supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(false)
        }

        val viewModel = ViewModelProviders.of(this).get(BackupMnemonicViewModel::class.java)
        viewModel.openWallet(walletId, password, seed)

        dot1.setImageDrawable(BackgroundHelper.getDotDrawable(this, R.color.color_FFFFFF, dp2px(6)))
        dot2.setImageDrawable(BackgroundHelper.getDotDrawable(this, R.color.color_FFFFFF, dp2px(6)))

        next.background = BackgroundHelper.getButtonBackground(this)

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        val list = mutableListOf<String>()
        val adapter = MnemonicAdapter(list)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(SpaceItemDecoration(dp2px(1), dp2px(1), dp2px(1), dp2px(1)))

        next.setOnClickListener {
            startActivity(Intent(this, VerifyMnemonicActivity::class.java).apply { putExtra("seed", list.toTypedArray()) })
        }

        viewModel.title.observe(this, Observer { value -> value?.let { setCenterTitle(it) } })
        viewModel.nextVisibility.observe(this, Observer { value -> value?.let { next.visibility = it } })

        viewModel.seedList.observe(this, Observer { value ->
            value?.let {
                list.clear()
                list.addAll(it)
                adapter.notifyDataSetChanged()
            }
        })

        viewModel.showBackupDialog.observe(this, Observer { BakMnemonicDialog.display(supportFragmentManager) {} })

        viewModel.showPasswordDialog.observe(this, Observer {
            PasswordDialog.display(supportFragmentManager, walletId, false) {
                viewModel.openWallet(walletId, it)
            }
        })

        viewModel.showLoading.observe(this, Observer { showLoading() })
        viewModel.hideLoading.observe(this, Observer { hideLoading() })
        viewModel.toast.observe(this, Observer { toast(it) })
        viewModel.toastRes.observe(this, Observer { toast(it) })

        setRightIcon(R.drawable.icon_copy_black)
        setRightIconClick(View.OnClickListener { copy(list.joinToString(" ")) })

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event != null && event.action == KeyEvent.ACTION_DOWN) {
            if (walletId >= 0 && !password.isNullOrBlank() && seed.isNullOrBlank()) {
                finish()
            } else {
                toast(R.string.bak_mnemonic)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    class MnemonicAdapter(val data: List<String>) : RecyclerView.Adapter<MnemonicAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mnemonic, parent, false)
            view.background = BackgroundHelper.getBackground(parent.context, R.color.color_E7E9EC, dp2px(5))
            return ViewHolder(view)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindViewHolder(data[position], position)
        }

        class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
            fun bindViewHolder(mnemonic: String, position: Int) {
                tag.text = (position + 1).toString()
                title.text = mnemonic
            }
        }
    }
}
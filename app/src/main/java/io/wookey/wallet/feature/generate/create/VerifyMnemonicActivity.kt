package io.wookey.wallet.feature.generate.create

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.flexbox.*
import io.wookey.wallet.ActivityStackManager
import io.wookey.wallet.MainActivity
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.feature.wallet.WalletManagerActivity
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.remove
import io.wookey.wallet.support.extensions.sharedPreferences
import io.wookey.wallet.support.extensions.toast
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_verify_mnemonic.*
import kotlinx.android.synthetic.main.item_mnemonic_verify.*


class VerifyMnemonicActivity : BaseTitleSecondActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_verify_mnemonic)
        setCenterTitle(R.string.create_wallet)

        val seed = intent.getStringArrayExtra("seed")
        if (seed.size < 3) {
            finish()
            return
        }

        dot.setImageDrawable(BackgroundHelper.getDotDrawable(this, R.color.color_FFFFFF, dp2px(6)))

        mnemonic1.background = BackgroundHelper.getBackground(this, R.color.color_E7E9EC, dp2px(5))
        mnemonic2.background = BackgroundHelper.getBackground(this, R.color.color_E7E9EC, dp2px(5))
        mnemonic3.background = BackgroundHelper.getBackground(this, R.color.color_E7E9EC, dp2px(5))

        next.background = BackgroundHelper.getButtonBackground(this)

        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.alignItems = AlignItems.STRETCH
        layoutManager.justifyContent = JustifyContent.SPACE_BETWEEN

        val list = seed.toList()
        val positionList = mutableListOf<Int>()

        val indexList = mutableListOf<Int>()
        for (i in 0 until list.size) {
            indexList.add(i + 1)
        }
        val indexShuffled = indexList.shuffled().take(3)

        tag1.text = "#${indexShuffled[0]}"
        tag2.text = "#${indexShuffled[1]}"
        tag3.text = "#${indexShuffled[2]}"

        recyclerView.layoutManager = layoutManager
        val data = list.shuffled()
        val adapter = VerifyMnemonicAdapter(data, positionList) { value, position ->
            when {
                mnemonic1.text.isNullOrBlank() -> {
                    mnemonic1.text = value
                    positionList.add(0, position)
                    recyclerView.adapter?.notifyDataSetChanged()
                }
                mnemonic2.text.isNullOrBlank() -> {
                    mnemonic2.text = value
                    positionList.add(1, position)
                    recyclerView.adapter?.notifyDataSetChanged()
                }
                mnemonic3.text.isNullOrBlank() -> {
                    mnemonic3.text = value
                    positionList.add(2, position)
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            }
        }
        recyclerView.adapter = adapter

        next.setOnClickListener {
            if (list[indexShuffled[0] - 1] == mnemonic1.text
                && list[indexShuffled[1] - 1] == mnemonic2.text
                && list[indexShuffled[2] - 1] == mnemonic3.text
            ) {
                sharedPreferences().remove("walletId")
                if (ActivityStackManager.getInstance().contain(WalletManagerActivity::class.java)) {
                    ActivityStackManager.getInstance().finishToActivity(WalletManagerActivity::class.java)
                } else {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                    finish()
                }
            } else {
                toast(R.string.verify_failed)
                mnemonic1.text = ""
                mnemonic2.text = ""
                mnemonic3.text = ""
                positionList.clear()
                adapter.notifyDataSetChanged()
            }
        }

        mnemonic1.setOnClickListener {
            if (mnemonic1.text.isNullOrBlank()) {
                return@setOnClickListener
            }
            if (positionList.size != 1) {
                return@setOnClickListener
            }
            mnemonic1.text = ""
            positionList.removeAt(0)
            adapter.notifyDataSetChanged()
        }
        mnemonic2.setOnClickListener {
            if (mnemonic2.text.isNullOrBlank()) {
                return@setOnClickListener
            }
            if (positionList.size != 2) {
                return@setOnClickListener
            }
            mnemonic2.text = ""
            positionList.removeAt(1)
            adapter.notifyDataSetChanged()
        }
        mnemonic3.setOnClickListener {
            if (mnemonic3.text.isNullOrBlank()) {
                return@setOnClickListener
            }
            if (positionList.size != 3) {
                return@setOnClickListener
            }
            mnemonic3.text = ""
            positionList.removeAt(2)
            adapter.notifyDataSetChanged()
        }
    }

    class VerifyMnemonicAdapter(
        val data: List<String>,
        private val positionList: List<Int>,
        private val listener: (String, Int) -> Unit
    ) :
        RecyclerView.Adapter<VerifyMnemonicAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mnemonic_verify, parent, false)
            view.background = BackgroundHelper.getBackground(parent.context, R.color.color_F6F6F6, dp2px(5))
            val lp = view.layoutParams
            if (lp is FlexboxLayoutManager.LayoutParams) {
                lp.flexGrow = 1f
            }
            return ViewHolder(view, listener)
        }

        override fun getItemCount() = data.size


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindViewHolder(data[position], position, positionList)
        }

        class ViewHolder(override val containerView: View, private val listener: (String, Int) -> Unit) :
            RecyclerView.ViewHolder(containerView), LayoutContainer {

            fun bindViewHolder(mnemonic: String, position: Int, positionList: List<Int>) {
                title.text = mnemonic
                if (positionList.contains(position)) {
                    title.setTextColor(ContextCompat.getColor(title.context, R.color.color_DEDEDE))
                } else {
                    title.setTextColor(ContextCompat.getColor(title.context, R.color.color_505050))
                }
                itemView.setOnClickListener {
                    if (!positionList.contains(position)) {
                        listener(mnemonic, position)
                    }
                }
            }
        }
    }
}
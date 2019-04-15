package io.wookey.wallet.feature.coin

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.support.coinList
import io.wookey.wallet.data.entity.Coin
import io.wookey.wallet.feature.generate.GenerateWalletActivity
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.putString
import io.wookey.wallet.support.extensions.setImage
import io.wookey.wallet.support.extensions.sharedPreferences
import io.wookey.wallet.widget.SpaceItemDecoration
import kotlinx.android.synthetic.main.activity_select_coin.*
import kotlinx.android.synthetic.main.item_coin.view.*

class SelectCoinActivity : BaseTitleSecondActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_coin)

        setCenterTitle(R.string.coin_select)

        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = CoinAdapter(coinList) {
            startActivity(Intent(this, GenerateWalletActivity::class.java).apply {
                sharedPreferences().putString("symbol", it.symbol)
            })
        }
        recyclerView.addItemDecoration(SpaceItemDecoration(0, 0, 0, dp2px(10)))

    }

    class CoinAdapter(val data: List<Coin>, private val listener: (Coin) -> Unit) :
            RecyclerView.Adapter<CoinAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coin, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val coin = data[position]
            with(holder.itemView) {
                icon.setImage(coin.symbol)
                symbol.text = coin.symbol
                name.text = coin.coin
                setOnClickListener {
                    listener(coin)
                }
            }
        }

        class ViewHolder(item: View) : RecyclerView.ViewHolder(item)
    }
}
package io.wookey.wallet.feature.coin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSearchActivity
import io.wookey.wallet.support.coinList
import io.wookey.wallet.data.entity.Coin
import io.wookey.wallet.support.extensions.afterTextChanged
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.setImage
import io.wookey.wallet.widget.DividerItemDecoration
import io.wookey.wallet.widget.StatusAdapterWrapper
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_search_coin.*
import kotlinx.android.synthetic.main.item_common.*
import java.util.regex.Pattern

class SearchCoinActivity : BaseTitleSearchActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_coin)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val list = mutableListOf<Coin>()
        list.addAll(coinList)
        val adapter = SearchCoinAdapter(list) {
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("symbol", it.symbol)
            })
            finish()
        }
        val wrapper = object : StatusAdapterWrapper(adapter) {
            override fun getEmptyImageResource(): Int {
                return R.drawable.icon_search_failed
            }

            override fun getEmptyStringResource(): Int {
                return R.string.search_failed
            }
        }
        wrapper.setSuccessView()
        recyclerView.adapter = wrapper
        recyclerView.addItemDecoration(DividerItemDecoration().apply {
            setOrientation(DividerItemDecoration.VERTICAL)
            setMarginStart(dp2px(25))
        })

        search.afterTextChanged { value ->
            list.clear()
            list.addAll(coinList.filter {
                if (value.isNullOrBlank()) {
                    true
                } else {
                    val compile = Pattern.compile(value, Pattern.CASE_INSENSITIVE)
                    compile.matcher(it.symbol).find() || compile.matcher(it.coin).find()
                }
            })
            adapter.notifyDataSetChanged()
            wrapper.setSuccessView()
        }
    }

    class SearchCoinAdapter(val data: List<Coin>, private val listener: (Coin) -> Unit) : RecyclerView.Adapter<SearchCoinAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_common, parent, false)
            return ViewHolder(view, listener)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindViewHolder(data[position])
        }

        class ViewHolder(override val containerView: View, val listener: (Coin) -> Unit) : RecyclerView.ViewHolder(containerView), LayoutContainer {

            fun bindViewHolder(coin: Coin) {
                with(coin) {
                    icon.setImage(symbol)
                    title.text = symbol
                    subTitle.text = this.coin
                    itemView.setOnClickListener {
                        listener(this)
                    }
                }
            }
        }
    }
}
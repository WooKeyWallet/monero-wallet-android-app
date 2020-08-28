package io.wookey.wallet.feature.swap

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.data.entity.SwapRecord
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.formatterDate
import io.wookey.wallet.support.extensions.setOnLoadMoreListener
import io.wookey.wallet.support.extensions.viewModel
import io.wookey.wallet.widget.DividerItemDecoration
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_swap_records.*
import kotlinx.android.synthetic.main.item_swap_record.*

class SwapRecordsActivity : BaseTitleSecondActivity() {

    val viewModel by viewModel<SwapRecordsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swap_records)

        setCenterTitle(R.string.swap_records)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration().apply {
            setOrientation(DividerItemDecoration.VERTICAL)
            setMarginStart(dp2px(25))
        })
        val list = mutableListOf<SwapRecord>()
        val adapter = SwapRecordsAdapter(list) {
            startActivity(Intent(this, SwapDetailActivity::class.java).apply {
                putExtra("id", it.swapId)
            })
        }
        recyclerView.adapter = adapter
        recyclerView.setOnLoadMoreListener {
            viewModel.loadMore()
        }

        viewModel.records.observe(this, Observer {
            list.clear()
            list.addAll(it ?: emptyList())
            adapter.notifyDataSetChanged()
            if (list.isNullOrEmpty()) {
                placeholderGroup.visibility = View.VISIBLE
            } else {
                placeholderGroup.visibility = View.GONE
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    class SwapRecordsAdapter(
        val data: List<SwapRecord>,
        private val listener: (SwapRecord) -> Unit
    ) :
        RecyclerView.Adapter<SwapRecordsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_swap_record, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindViewHolder(data[position], listener)
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val fromAmount: TextView = itemView.findViewById(R.id.fromAmount)
            private val time: TextView = itemView.findViewById(R.id.time)
            private val toAmount: TextView = itemView.findViewById(R.id.toAmount)

            fun bindViewHolder(swapRecord: SwapRecord, listener: (SwapRecord) -> Unit) {
                fromAmount.text =
                    "${swapRecord.amountFrom} ${swapRecord.currencyFrom.toUpperCase()}"
                time.text =
                    swapRecord.createdAt.toLongOrNull()?.times(1000)?.formatterDate() ?: "--"
                toAmount.text = "${swapRecord.amountTo} ${swapRecord.currencyTo.toUpperCase()}"
                itemView.setOnClickListener {
                    listener.invoke(swapRecord)
                }
            }
        }
    }
}
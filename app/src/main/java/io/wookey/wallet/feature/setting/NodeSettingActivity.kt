package io.wookey.wallet.feature.setting

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.Node
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.setImage
import io.wookey.wallet.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_node_setting.*
import kotlinx.android.synthetic.main.item_node_setting.view.*

class NodeSettingActivity : BaseTitleSecondActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_node_setting)

        setCenterTitle(R.string.node_setting)

        val viewModel = ViewModelProviders.of(this).get(NodeSettingViewModel::class.java)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val list = mutableListOf<Node>()

        val adapter = NodeSettingAdapter(list) {
            startActivity(Intent(this, NodeListActivity::class.java).apply {
                putExtra("symbol", it.symbol)
            })
        }
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration().apply {
            setOrientation(DividerItemDecoration.VERTICAL)
            setMarginStart(dp2px(25))
        })

        AppDatabase.getInstance().nodeDao().loadSelectedNodes().observe(this, Observer { value ->
            value?.let {
                list.clear()
                list.addAll(it)
                adapter.notifyDataSetChanged()
            }
        })
    }

    class NodeSettingAdapter(val data: List<Node>, private val listener: (Node) -> Unit) : RecyclerView.Adapter<NodeSettingAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_node_setting, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val node = data[position]
            with(holder.itemView) {
                icon.setImage(node.symbol)
                name.text = node.symbol
                url.text = node.url
                setOnClickListener {
                    listener(node)
                }
            }
        }

        class ViewHolder(item: View) : RecyclerView.ViewHolder(item)
    }
}
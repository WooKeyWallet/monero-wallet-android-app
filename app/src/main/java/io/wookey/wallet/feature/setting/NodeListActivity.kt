package io.wookey.wallet.feature.setting

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.Node
import io.wookey.wallet.dialog.NodeEditDialog
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.openBrowser
import io.wookey.wallet.support.extensions.toast
import io.wookey.wallet.widget.DividerItemDecoration
import io.wookey.wallet.widget.IOSDialog
import kotlinx.android.synthetic.main.activity_node_list.*
import kotlinx.android.synthetic.main.item_node.view.*

class NodeListActivity : BaseTitleSecondActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_node_list)

        val viewModel = ViewModelProviders.of(this).get(NodeListViewModel::class.java)

        val symbol = "XMR"
        val canDelete = intent.getBooleanExtra("canDelete", true)
        viewModel.setCanDelete(canDelete)
        setCenterTitle("$symbol ${getString(R.string.node_setting)}")
        setRightIcon(R.drawable.icon_add)
        setRightIconClick(View.OnClickListener { _ ->
            NodeEditDialog.display(supportFragmentManager, symbol) { value ->
                value?.let {
                    viewModel.insertNode(it)
                }
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        val list = mutableListOf<Node>()
        val adapter = NodeAdapter(list, viewModel)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration().apply {
            setOrientation(DividerItemDecoration.VERTICAL)
            setMarginStart(dp2px(25))
        })
        recyclerView.isNestedScrollingEnabled = false

        AppDatabase.getInstance().nodeDao().loadSymbolNodes(symbol).observe(this, Observer { value ->
            value?.let {
                list.clear()
                list.addAll(it)
                adapter.notifyDataSetChanged()
                viewModel.testRpcService(list)
            }
        })

        viewModel.toastRes.observe(this, Observer { toast(it) })
        viewModel.showConfirmDialog.observe(this, Observer { value ->
            value?.let {
                IOSDialog(this)
                        .radius(dp2px(5))
                        .titleText(getString(R.string.confirm_delete))
                        .contentText(getString(R.string.confirm_delete_content, it))
                        .leftText(getString(R.string.cancel))
                        .rightText(getString(R.string.confirm))
                        .setIOSDialogLeftListener { viewModel.cancelDelete() }
                        .setIOSDialogRightListener { viewModel.confirmDelete(symbol) }
                        .cancelAble(true)
                        .layout()
                        .show()
            }
        })

        viewModel.showLoading.observe(this, Observer { showLoading() })
        viewModel.hideLoading.observe(this, Observer { hideLoading() })
        viewModel.toast.observe(this, Observer { toast(it) })
        viewModel.dataChanged.observe(this, Observer { adapter.notifyDataSetChanged() })

        more.setOnClickListener {
            openBrowser("https://wallet.wookey.io/monero-nodes/app.html")
        }

        viewModel.finish.observe(this, Observer {
            setResult(Activity.RESULT_OK, Intent().apply { putExtra("node", it) })
            finish()
        })
    }

    class NodeAdapter(val data: List<Node>, val viewModel: NodeListViewModel) : RecyclerView.Adapter<NodeAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_node, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val node = data[position]
            with(holder.itemView) {
                title.text = node.url
                if (node.isSelected) {
                    selected.setImageResource(R.drawable.icon_selected)
                } else {
                    selected.setImageResource(R.drawable.icon_unselected)
                }
                if (node.responseTime != Long.MAX_VALUE && node.responseTime >= 0) {
                    subTitle.text = "${node.responseTime} ms"
                } else {
                    subTitle.text = ""
                }
                if (node.responseTime < 1000) {
                    subTitle.setTextColor(ContextCompat.getColor(subTitle.context, R.color.color_26B479))
                } else {
                    subTitle.setTextColor(ContextCompat.getColor(subTitle.context, R.color.color_FF3A5C))
                }
                setOnClickListener {
                    viewModel.updateNode(data, node)
                }
                setOnLongClickListener {
                    viewModel.onLongClick(node)
                    true
                }
            }
        }

        class ViewHolder(item: View) : RecyclerView.ViewHolder(item)
    }
}
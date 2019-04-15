package io.wookey.wallet.feature.setting

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.wookey.wallet.ActivityStackManager
import io.wookey.wallet.MainActivity
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.support.extensions.*
import io.wookey.wallet.widget.DividerItemDecoration
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_language.*
import kotlinx.android.synthetic.main.item_language.*

class LanguageActivity : BaseTitleSecondActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)
        setCenterTitle(R.string.select_language)

        recyclerView.layoutManager = LinearLayoutManager(this)
        val list = listOf("zh-CN", "en")
        val adapter = LanguageAdapter(list) {
            setLocale(this, it)
            recreate()
            ActivityStackManager.getInstance().get(MainActivity::class.java)?.recreate()
        }
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration().apply {
            setOrientation(DividerItemDecoration.VERTICAL)
            setMarginStart(dp2px(25))
        })
    }

    class LanguageAdapter(val data: List<String>, val listener: (String) -> Unit) :
        RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false)
            return ViewHolder(view, listener)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindViewHolder(data[position])
        }

        class ViewHolder(override val containerView: View, val listener: (String) -> Unit) :
            RecyclerView.ViewHolder(containerView), LayoutContainer {
            fun bindViewHolder(lang: String) {
                content.text = getDisplayName(lang)
                if (containerView.context.isSelectedLanguage(lang)) {
                    selected.setImageResource(R.drawable.icon_selected)
                } else {
                    selected.setImageResource(R.drawable.icon_unselected)
                }
                itemView.setOnClickListener { listener(lang) }
            }
        }
    }
}
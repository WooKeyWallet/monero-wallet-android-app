package io.wookey.wallet.base

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import io.wookey.wallet.R
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.hideKeyboard
import io.wookey.wallet.support.utils.DrawableHelper

open class BaseTitleSearchActivity : BaseTitleActivity() {

    lateinit var toolBar: Toolbar
    lateinit var search: EditText
    lateinit var cancel: TextView
    lateinit var leftIcon: ImageView

    override fun addTitle(): View {
        val title = View.inflate(this, R.layout.base_title_search, null)
        toolBar = title.findViewById(R.id.toolbar)
        setSupportActionBar(toolBar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        search = title.findViewById(R.id.search)
        search.background = DrawableHelper.getSolidShapeDrawable(ContextCompat.getColor(this, R.color.color_E7E9EC), dp2px(100f))

        cancel = title.findViewById(R.id.cancel)
        cancel.setOnClickListener { v -> onRightClickListener(v) }

        leftIcon = title.findViewById(R.id.leftIcon)
        return title
    }

    open fun setSearchHint(@StringRes hint: Int) {
        search.setHint(hint)
    }

    open fun setSearchHint(hint: String) {
        search.hint = hint
    }

    open fun onRightClickListener(v: View) {
        finish()
    }

    open fun setLeftIcon(@DrawableRes icon: Int) {
        leftIcon.visibility = View.VISIBLE
        cancel.visibility = View.GONE
        leftIcon.setImageResource(icon)
    }

    open fun setLeftIconClick(listener: View.OnClickListener) {
        leftIcon.visibility = View.VISIBLE
        cancel.visibility = View.GONE
        leftIcon.setOnClickListener(listener)
    }

    open fun setSearchMargin(left: Int, top: Int, right: Int, bottom: Int) {
        val lp = search.layoutParams as Toolbar.LayoutParams
        lp.marginStart = left
        lp.topMargin = top
        lp.marginEnd = right
        lp.bottomMargin = bottom
        search.layoutParams = lp
    }

    override fun finish() {
        toolBar.hideKeyboard()
        super.finish()
    }
}
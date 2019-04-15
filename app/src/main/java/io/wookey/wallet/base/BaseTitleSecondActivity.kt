package io.wookey.wallet.base

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.wookey.wallet.R
import io.wookey.wallet.support.extensions.hideKeyboard

open class BaseTitleSecondActivity : BaseTitleActivity() {

    lateinit var toolBar: Toolbar
    lateinit var centerTitle: TextView
    lateinit var rightTitle: TextView
    lateinit var leftIcon: ImageView
    lateinit var rightIcon: ImageView

    override fun addTitle(): View? {
        val title = View.inflate(this, R.layout.base_title_second, null)
        toolBar = title.findViewById(R.id.toolbar)
        setSupportActionBar(toolBar)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        toolBar.setNavigationOnClickListener { v -> onNavigationClick(v) }
        centerTitle = title.findViewById(R.id.centerTitle)
        rightTitle = title.findViewById(R.id.rightTitle)
        leftIcon = title.findViewById(R.id.leftIcon)
        rightIcon = title.findViewById(R.id.rightIcon)
        return title
    }

    open fun onNavigationClick(v: View?) {
        finish()
    }

    open fun setNavigationIcon(@DrawableRes icon: Int) {
        toolBar.setNavigationIcon(icon)
    }

    open fun setToolbarBackgroundColor(@ColorRes color: Int) {
        toolBar.setBackgroundColor(ContextCompat.getColor(this, color))
    }

    open fun setToolbarBackgroundDrawable(@DrawableRes drawable: Int) {
        toolBar.background = ContextCompat.getDrawable(this, drawable)
    }

    open fun setToolbarHeight(height: Int) {
        val lp = toolBar.layoutParams
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
        lp.height = height
        toolBar.layoutParams = lp
    }

    open fun setCenterTitle(title: String) {
        centerTitle.text = title
    }

    open fun setCenterTitle(@StringRes title: Int) {
        centerTitle.setText(title)
    }

    open fun setCenterTitleColor(@ColorRes color: Int) {
        centerTitle.setTextColor(ContextCompat.getColor(this, color))
    }

    open fun setRightTitle(title: String) {
        rightTitle.visibility = View.VISIBLE
        rightTitle.text = title
    }

    open fun setRightTitle(@StringRes title: Int) {
        rightTitle.visibility = View.VISIBLE
        rightTitle.setText(title)
    }

    open fun setRightTitleClick(listener: View.OnClickListener) {
        rightTitle.visibility = View.VISIBLE
        rightTitle.setOnClickListener(listener)
    }

    open fun setRightIcon(@DrawableRes icon: Int) {
        rightIcon.visibility = View.VISIBLE
        rightIcon.setImageResource(icon)
    }

    open fun setRightIconClick(listener: View.OnClickListener) {
        rightIcon.visibility = View.VISIBLE
        rightIcon.setOnClickListener(listener)
    }

    open fun setLeftIcon(@DrawableRes icon: Int) {
        leftIcon.visibility = View.VISIBLE
        leftIcon.setImageResource(icon)
    }

    open fun setLeftIconClick(listener: View.OnClickListener) {
        leftIcon.visibility = View.VISIBLE
        leftIcon.setOnClickListener(listener)
    }

    override fun finish() {
        toolBar.hideKeyboard()
        super.finish()
    }
}
package io.wookey.wallet.base

import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import io.wookey.wallet.R
import io.wookey.wallet.support.utils.DrawableHelper
import io.wookey.wallet.support.utils.StatusBarHelper

open class BaseTitleActivity : BaseActivity() {

    private lateinit var mContentView: View

    override fun setContentView(layoutResID: Int) {
        val view = View.inflate(this, layoutResID, null)
        setContentView(view)
    }

    override fun setContentView(view: View) {
        mContentView = view
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        val title = addTitle()
        if (title != null) {
            linearLayout.addView(title)
        }
        val divider = addDivider()
        if (divider != null) {
            linearLayout.addView(divider)
        }
        val params =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        view.layoutParams = params
        linearLayout.addView(view)
        linearLayout.clipChildren = clipChildren()
        super.setContentView(linearLayout)

        setStatusBarLightMode()
    }

    open fun setStatusBarLightMode() {
        StatusBarHelper.setStatusBarLightMode(this)
    }

    open fun addTitle(): View? {
        return null
    }

    open fun clipChildren(): Boolean {
        return true
    }

    /**
     * 二级页面分割线
     */
    open fun addDivider(): View? {
        val drawable = DrawableHelper.getSolidShapeDrawable(getDividerColor(), 0f)
        val imageView = ImageView(this)
        imageView.minimumHeight = 1
        imageView.maxHeight = 1
        imageView.setImageDrawable(drawable)
        return imageView
    }

    open fun getDividerColor(): Int {
        return ContextCompat.getColor(this, R.color.color_DEDEDE)
    }

    open fun getTarget(): View? {
        return mContentView
    }
}
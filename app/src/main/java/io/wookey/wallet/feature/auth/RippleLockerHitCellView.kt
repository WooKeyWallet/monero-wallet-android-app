package io.wookey.wallet.feature.auth

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import com.github.ihsg.patternlocker.CellBean
import com.github.ihsg.patternlocker.IHitCellView
import io.wookey.wallet.support.extensions.dp2px

class RippleLockerHitCellView : IHitCellView {

    @ColorInt
    private var hitColor: Int = 0xff00bc6d.toInt()

    @ColorInt
    private var errorColor: Int = 0xffff3a5c.toInt()

    private val paint: Paint = Paint()

    init {
        paint.isDither = true
        paint.isAntiAlias = true
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        this.paint.style = Paint.Style.FILL
    }

    @ColorInt
    fun getHitColor(): Int {
        return hitColor
    }

    fun setHitColor(@ColorInt hitColor: Int): RippleLockerHitCellView {
        this.hitColor = hitColor
        return this
    }

    @ColorInt
    fun getErrorColor(): Int {
        return errorColor
    }

    fun setErrorColor(@ColorInt errorColor: Int): RippleLockerHitCellView {
        this.errorColor = errorColor
        return this
    }

    override fun draw(canvas: Canvas, cellBean: CellBean, isError: Boolean) {
        val saveCount = canvas.save()

        this.paint.color = getColor(isError)
        this.paint.alpha = 38
        canvas.drawCircle(cellBean.x, cellBean.y, dp2px(30f), this.paint)

        this.paint.color = getColor(isError)
        this.paint.alpha = 255
        canvas.drawCircle(cellBean.x, cellBean.y, dp2px(8f), this.paint)

        canvas.restoreToCount(saveCount)
    }

    @ColorInt
    private fun getColor(isError: Boolean): Int {
        return if (isError) this.getErrorColor() else this.getHitColor()
    }
}
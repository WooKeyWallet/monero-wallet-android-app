package io.wookey.wallet.feature.auth

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import com.github.ihsg.patternlocker.CellBean
import com.github.ihsg.patternlocker.IHitCellView
import com.github.ihsg.patternlocker.INormalCellView

class IndicatorHitCellView : IHitCellView {

    @ColorInt
    private var hitColor: Int = 0xff00bc6d.toInt()

    @ColorInt
    private var errorColor: Int = 0xffff3a5c.toInt()

    private var radius: Float = 4f

    @ColorInt
    fun getHitColor(): Int {
        return hitColor
    }

    fun setHitColor(@ColorInt hitColor: Int): IndicatorHitCellView {
        this.hitColor = hitColor
        return this
    }

    @ColorInt
    fun getErrorColor(): Int {
        return errorColor
    }

    fun setErrorColor(@ColorInt errorColor: Int): IndicatorHitCellView {
        this.errorColor = errorColor
        return this
    }

    fun getRadius(): Float {
        return radius
    }

    fun setRadius(radius: Float): IndicatorHitCellView {
        this.radius = radius
        return this
    }

    private val paint: Paint = Paint()

    init {
        paint.isDither = true
        paint.isAntiAlias = true
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        this.paint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas, cellBean: CellBean, isError: Boolean) {
        val saveCount = canvas.save()
        this.paint.color = if (isError) getErrorColor() else getHitColor()
        canvas.drawCircle(cellBean.x, cellBean.y, radius, this.paint)
        canvas.restoreToCount(saveCount)
    }
}
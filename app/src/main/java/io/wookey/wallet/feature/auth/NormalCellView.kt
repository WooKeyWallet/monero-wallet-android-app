package io.wookey.wallet.feature.auth

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import com.github.ihsg.patternlocker.CellBean
import com.github.ihsg.patternlocker.INormalCellView

class NormalCellView : INormalCellView {

    @ColorInt
    private var normalColor: Int = 0xffd5dae1.toInt()

    private var radius: Float = 8f

    @ColorInt
    fun getNormalColor(): Int {
        return normalColor
    }

    fun setNormalColor(@ColorInt normalColor: Int): NormalCellView {
        this.normalColor = normalColor
        return this
    }

    fun getRadius(): Float {
        return radius
    }

    fun setRadius(radius: Float): NormalCellView {
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

    override fun draw(canvas: Canvas, cellBean: CellBean) {
        val saveCount = canvas.save()
        this.paint.color = getNormalColor()
        canvas.drawCircle(cellBean.x, cellBean.y, radius, this.paint)
        canvas.restoreToCount(saveCount)
    }
}
package io.wookey.wallet.support

import android.text.InputFilter
import android.text.Spanned

class LengthFilter(private val maxLen: Int) : InputFilter {

    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence {

        // 新输入的字符串为空直接不接收（删除剪切等）
        if (source.isNullOrEmpty()) return ""

        // 输入前就存在的字符长度
        val destCount = getCount(dest.toString())

        // 输入前就已满直接不接收
        if (destCount >= maxLen) return ""

        // 新输入的字符长度
        val sourceCount = getCount(source.toString())

        // 如果拼接后不超长，直接拼接
        if (destCount + sourceCount <= maxLen) return source

        // 超长时不应该直接拒绝，应在允许范围内尽量拼接
        return getByCount(source.toString(), maxLen - destCount)
    }

    /**
     * 超长时根据剩余长度在字符范围内截取字符串
     */
    private fun getByCount(s: String, count: Int): String {
        var temp = ""
        var tempCount = 0

        val cs = s.toCharArray()
        for (c in cs) {
            if (tempCount + getCount(c) <= count) {
                tempCount += getCount(c)
                temp += c
            } else {
                break
            }
        }

        return temp
    }

    /**
     * 计算字符串长度
     */
    private fun getCount(s: String?): Int {
        if (s.isNullOrEmpty()) return 0
        var count = 0
        s.toCharArray().forEach {
            count += getCount(it)
        }
        return count
    }

    /**
     * 单字符占位判定
     */
    private fun getCount(c: Char) = if (c.toString().toByteArray().size > 2) 2 else 1
}
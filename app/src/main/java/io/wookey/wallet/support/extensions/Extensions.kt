package io.wookey.wallet.support.extensions

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.LocaleList
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.*
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bigkoo.pickerview.builder.TimePickerBuilder
import io.wookey.wallet.App
import io.wookey.wallet.R
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

fun AppCompatActivity.toast(msg: String?) {
    if (msg.isNullOrBlank()) {
        return
    }
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.toast(stringRes: Int?) {
    if (stringRes == null) {
        return
    }
    Toast.makeText(this, getString(stringRes), Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.copy(value: String?) {
    if (value.isNullOrBlank()) {
        return
    }
    val cmb = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    if (cmb != null) {
        cmb.primaryClip = ClipData.newPlainText(null, value)
        toast(R.string.copy_success)
    }
}

fun Fragment.toast(msg: String?) {
    if (msg.isNullOrBlank()) {
        return
    }
    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(stringRes: Int?) {
    if (stringRes == null) {
        return
    }
    Toast.makeText(activity, getString(stringRes), Toast.LENGTH_SHORT).show()
}

fun Fragment.copy(value: String?) {
    if (value.isNullOrBlank()) {
        return
    }
    val cmb = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    if (cmb != null) {
        cmb.primaryClip = ClipData.newPlainText(null, value)
        toast(R.string.copy_success)
    }
}

fun Context.versionName(): String {
    val packageManager = packageManager
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    return packageInfo.versionName ?: ""
}

fun AppCompatActivity.versionName(): String {
    return applicationContext.versionName()
}

fun Fragment.versionName(): String {
    return context?.versionName() ?: ""
}

fun Context.versionCode(): Int {
    val packageManager = packageManager
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    return packageInfo.versionCode
}

fun AppCompatActivity.versionCode(): Int {
    return applicationContext.versionCode()
}

fun Fragment.versionCode(): Int {
    return context?.versionCode() ?: 0
}

fun dp2px(dp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics)
}

fun dp2px(dp: Int): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), Resources.getSystem().displayMetrics)
        .toInt()
}

fun screenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(windowToken, 0)
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            s?.also {
                afterTextChanged(s.toString().trim())
            } ?: afterTextChanged("")
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

// TODO 增加图标字段，存储图标名称，用反射查找
fun ImageView.setImage(coin: String) {
    when (coin.toUpperCase()) {
        "XMR" -> setImageResource(R.drawable.icon_xmr)
        "DASH" -> setImageResource(R.drawable.icon_dash)
    }
}

fun View.setRateHeight(rate: Float, width: Int): View {
    val params = layoutParams
    params.width = width
    params.height = ((width / rate).toInt())
    layoutParams = params
    return this
}

fun Date.formatterDate(): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    return format.format(this)
}

fun Long.formatterDate(): String {
    return formatterDate(this, "yyyy-MM-dd HH:mm:ss")
}

fun formatterDate(timestamp: Long, pattern: String): String {
    try {
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.CHINA)
        return simpleDateFormat.format(Date(timestamp))
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "--"
}

fun String.formatterAmount(digit: Int = 12): String {
    if (isNullOrBlank()) {
        return "0.00"
    }
    try {
        var value = BigDecimal(this).stripTrailingZeros().toPlainString()
        val split = value.split(".")
        if (split.size == 2) {
            value = if (split[1].length > digit) {
                split[0] + "." + split[1].substring(0, digit)
            } else {
                val stringBuffer = StringBuffer(split[0] + "." + split[1])
                for (i in 0 until digit - split[1].length) {
                    stringBuffer.append("0")
                }
                stringBuffer.toString()
            }
        } else if (split.size == 1) {
            val stringBuffer = StringBuffer(split[0] + ".")
            for (i in 0 until digit) {
                stringBuffer.append("0")
            }
            value = stringBuffer.toString()
        }
        return value
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "0.00"
}

fun String.formatterAmountStrip(): String {
    if (isNullOrBlank()) {
        return "0.00"
    }
    try {
        val bigDecimal = BigDecimal(this)
        if (bigDecimal.compareTo(BigDecimal("0.00")) == 0) {
            return "0.00"
        }
        var value = bigDecimal.stripTrailingZeros().toPlainString()
        val split = value.split(".")
        if (split.size == 2) {
            value = if (split[1].length >= 2) {
                split[0] + "." + split[1]
            } else {
                val stringBuffer = StringBuffer(split[0] + "." + split[1])
                for (i in 0 until 2 - split[1].length) {
                    stringBuffer.append("0")
                }
                stringBuffer.toString()
            }
        } else if (split.size == 1) {
            value = split[0] + ".00"
        }
        return value
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "0.00"
}

fun SpannableString.foregroundColorSpan(range: IntRange, color: Int) {
    setSpan(ForegroundColorSpan(color), range.start, range.endInclusive, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
}

fun SpannableString.clickableSpan(range: IntRange, color: Int, listener: (View) -> Unit) {
    setSpan(object : ClickableSpan() {
        override fun onClick(widget: View) {
            listener(widget)
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.color = color
            ds.isUnderlineText = false
            ds.clearShadowLayer()
        }
    }, range.start, range.endInclusive, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
}

fun View.showTimePicker(
    startDate: Calendar = Calendar.getInstance().apply { set(2014, 4, 1) },
    listener: (Date) -> Unit
) {
    setOnClickListener {
        hideKeyboard()
        TimePickerBuilder(context) { date, v -> listener(date) }
            .setRangDate(startDate, Calendar.getInstance())
            .setLabel("", "", "", "", "", "")
            .build()
            .show()
    }
}

fun getSystemDefaultLocale(): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocaleList.getDefault().get(0)
    } else {
        Locale.getDefault()
    }
}

fun getSystemDefaultLocale(configuration: Configuration): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        configuration.locales[0]
    } else {
        configuration.locale
    }
}

fun Context.getLocale() = sharedPreferences().getString("localeLanguage", "") ?: ""

fun setLocale(context: Context?, locale: String): Context? {
    if (context == null) {
        return context
    }
    sharedPreferences().putString("localeLanguage", locale)
    val newLocale = when (locale) {
        "zh-CN" -> Locale.SIMPLIFIED_CHINESE
        "en" -> Locale.ENGLISH
        else -> App.SYSTEM_DEFAULT_LOCALE
    }
    val configuration = context.resources.configuration
    Locale.setDefault(newLocale)
    configuration.setLocale(newLocale)
    configuration.setLayoutDirection(newLocale)
    return context.createConfigurationContext(configuration)
}

fun Context.isSelectedLanguage(lang: String): Boolean {
    var locale = getLocale()
    if (locale.isNullOrBlank()) {
        locale = if (App.SYSTEM_DEFAULT_LOCALE.language == "zh"
            && App.SYSTEM_DEFAULT_LOCALE.country == "CN"
        ) {
            "zh-CN"
        } else {
            "en"
        }
    }
    return lang == locale
}

fun Context.getCurrentLocale(): String {
    var locale = getLocale()
    if (locale.isNullOrBlank()) {
        locale = if (App.SYSTEM_DEFAULT_LOCALE.language == "zh"
            && App.SYSTEM_DEFAULT_LOCALE.country == "CN"
        ) {
            "zh-CN"
        } else {
            "en"
        }
    }
    return locale
}

fun getDisplayName(lang: String): String {
    return when (lang) {
        "zh-CN" -> "简体中文"
        "en" -> "English"
        else -> "English"
    }
}

fun Activity.openBrowser(url: String) {
    //从其他浏览器打开
    val intent = Intent().apply {
        action = Intent.ACTION_VIEW
        data = Uri.parse(url)
    }
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}
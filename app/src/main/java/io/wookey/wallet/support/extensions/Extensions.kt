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
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.text.*
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bigkoo.pickerview.builder.TimePickerBuilder
import io.wookey.wallet.App
import io.wookey.wallet.R
import io.wookey.wallet.data.remote.entity.RPCResponse
import io.wookey.wallet.support.KEY_ALIAS
import io.wookey.wallet.support.RSA_KEY_ALIAS
import java.math.BigDecimal
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.AlgorithmParameterSpec
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.security.auth.x500.X500Principal
import kotlin.math.max

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
        cmb.setPrimaryClip(ClipData.newPlainText(null, value))
        toast(R.string.copy_success)
    }
}

fun androidx.fragment.app.Fragment.toast(msg: String?) {
    if (msg.isNullOrBlank()) {
        return
    }
    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
}

fun androidx.fragment.app.Fragment.toast(stringRes: Int?) {
    if (stringRes == null) {
        return
    }
    Toast.makeText(activity, getString(stringRes), Toast.LENGTH_SHORT).show()
}

fun androidx.fragment.app.Fragment.copy(value: String?) {
    if (value.isNullOrBlank()) {
        return
    }
    val cmb = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    if (cmb != null) {
        cmb.setPrimaryClip(ClipData.newPlainText(null, value))
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

fun androidx.fragment.app.Fragment.versionName(): String {
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

fun androidx.fragment.app.Fragment.versionCode(): Int {
    return context?.versionCode() ?: 0
}

fun dp2px(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        Resources.getSystem().displayMetrics
    )
}

fun dp2px(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        Resources.getSystem().displayMetrics
    )
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
    setSpan(
        ForegroundColorSpan(color),
        range.start,
        range.endInclusive,
        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
    )
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

fun generateSecretKey() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setInvalidatedByBiometricEnrollment(true)
        }
        val keyGenParameterSpec = builder.build()
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
}

fun getSecretKey(): SecretKey {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)
    if (keyStore.getKey(KEY_ALIAS, null) == null) {
        generateSecretKey()
    }
    return keyStore.getKey(KEY_ALIAS, null) as SecretKey
}

@RequiresApi(Build.VERSION_CODES.M)
fun getCipher(): Cipher {
    return Cipher.getInstance(
        KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7
    )
}

fun generateRSAKey(context: Context) {
    val start: Calendar = GregorianCalendar()
    val end: Calendar = GregorianCalendar()
    end.add(Calendar.YEAR, 50)
    val kpGenerator: KeyPairGenerator = KeyPairGenerator
        .getInstance("RSA", "AndroidKeyStore")
    val spec: AlgorithmParameterSpec
    spec = KeyPairGeneratorSpec.Builder(context)
        .setAlias(RSA_KEY_ALIAS)
        .setSubject(X500Principal("CN=$RSA_KEY_ALIAS"))
        .setSerialNumber(BigInteger.valueOf(1337))
        .setStartDate(start.time)
        .setEndDate(end.time)
        .build()
    kpGenerator.initialize(spec)
    kpGenerator.generateKeyPair()
}

fun getRSAKey(context: Context): KeyStore.PrivateKeyEntry {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)
    if (keyStore.getEntry(RSA_KEY_ALIAS, null) == null) {
        generateRSAKey(context)
    }
    return keyStore.getEntry(RSA_KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
}

fun getRSACipher(): Cipher {
    return Cipher.getInstance("RSA/ECB/PKCS1Padding")
}

fun processRSAData(data: ByteArray, cipher: Cipher, isEncrypt: Boolean): ByteArray {
    val len = data.size
    var blockSize = cipher.blockSize
    blockSize = max(blockSize, 53)
    var outputSize = cipher.getOutputSize(blockSize)
    outputSize = max(outputSize, 64)
    val maxLen = if (isEncrypt) blockSize else outputSize
    val count = len / maxLen
    if (count > 0) {
        var ret = ByteArray(0)
        var buff = ByteArray(maxLen)
        var index = 0
        for (i in 0 until count) {
            System.arraycopy(data, index, buff, 0, maxLen)
            ret = joins(ret, cipher.doFinal(buff))
            index += maxLen
        }
        if (index != len) {
            val restLen = len - index
            buff = ByteArray(restLen)
            System.arraycopy(data, index, buff, 0, restLen)
            ret = joins(ret, cipher.doFinal(buff))
        }
        return ret
    } else {
        return cipher.doFinal(data)
    }
}

fun joins(prefix: ByteArray, suffix: ByteArray): ByteArray {
    val ret = ByteArray(prefix.size + suffix.size)
    System.arraycopy(prefix, 0, ret, 0, prefix.size)
    System.arraycopy(suffix, 0, ret, prefix.size, suffix.size)
    return ret
}

inline fun <reified VM : ViewModel> AppCompatActivity.viewModel(noinline factoryProducer: (() -> ViewModelProvider.Factory))
        : Lazy<VM> = lazy { factoryProducer.invoke().create(VM::class.java) }

inline fun <reified VM : ViewModel> AppCompatActivity.viewModel()
        : Lazy<VM> = lazy { ViewModelProviders.of(this).get(VM::class.java) }

inline fun <reified VM : ViewModel> Fragment.viewModel()
        : Lazy<VM> = lazy { ViewModelProviders.of(this).get(VM::class.java) }

inline fun <reified T> RPCResponse<T>.unWrap(): T? {
    if (this.error == null) {
        return this.result
    } else {
        throw IllegalStateException(this.error.message)
    }
}

fun String.decimalFormat(digit: Int = 8): String {
    return try {
        val formatter = DecimalFormat("#.${"#".repeat(digit)}")
        formatter.format(this.toDouble())
    } catch (e: Exception) {
        e.printStackTrace()
        "--"
    }
}

const val VISIBLE_THRESHOLD = 5
fun RecyclerView.setOnLoadMoreListener(loadMore: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager
            if (layoutManager is LinearLayoutManager) {
                val totalItemCount = layoutManager.itemCount
                val visibleItemCount = layoutManager.childCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (visibleItemCount + lastVisibleItem + VISIBLE_THRESHOLD >= totalItemCount) {
                    loadMore()
                }
            }
        }
    })
}

fun EditText.afterDecimalTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val trim = s?.toString()?.trim()
            if (trim != null && trim.startsWith("0") && trim.length > 1) {
                val index = trim.indexOf(".")
                if (index != 1) {
                    val dest = trim.toBigDecimal().stripTrailingZeros().toPlainString()
                    setText(dest)
                    setSelection(dest?.length ?: 0)
                } else {
                    afterTextChanged(trim)
                }
            } else {
                afterTextChanged(trim ?: "")
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

private fun formatInput(it: String?, input: EditText): Boolean {
    if (it != null && it.startsWith("0") && it.length > 1) {
        val index = it.indexOf(".")
        if (index != 1) {
            val decimal = BigDecimal(it).toPlainString()
            input.setText(decimal)
            return true
        }
    }
    return false
}

fun String.displayCoin(): String {
    if (this.equals("usdt", true)) {
        return "USDT(OMNI)"
    }
    if (this.equals("usdt20", true)) {
        return "USDT(ERC20)"
    }
    return this.toUpperCase(Locale.CHINA)
}

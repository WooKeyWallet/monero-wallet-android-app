package io.wookey.wallet.feature.setting

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.support.extensions.clickableSpan
import io.wookey.wallet.support.extensions.versionName
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : BaseTitleSecondActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setCenterTitle(R.string.about_us)

        version.text = getString(R.string.version_placeholder, versionName())

        val spanStr = getString(R.string.agreement)
        val s = "${getString(R.string.app_name)} $spanStr"
        val start = s.indexOf(spanStr)
        val end = start + spanStr.length
        val style = SpannableString(s)
        style.clickableSpan(start..end, ContextCompat.getColor(this, R.color.color_333333)) {
            startActivity(Intent(this, WebViewActivity::class.java))
        }
        agreement.text = style
        agreement.movementMethod = LinkMovementMethod.getInstance()
    }
}
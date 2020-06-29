package io.wookey.wallet.feature.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.core.content.ContextCompat
import com.github.ihsg.patternlocker.OnPatternChangeListener
import com.github.ihsg.patternlocker.PatternLockerView
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseActivity
import io.wookey.wallet.data.entity.WalletRelease
import io.wookey.wallet.dialog.PasswordDialog
import io.wookey.wallet.support.extensions.*
import io.wookey.wallet.support.utils.StatusBarHelper
import kotlinx.android.synthetic.main.activity_pattern_checking.*
import kotlinx.android.synthetic.main.activity_pattern_checking.msg
import kotlinx.android.synthetic.main.activity_pattern_checking.patternLockerView
import kotlinx.android.synthetic.main.activity_pattern_setting.*
import javax.crypto.Cipher

class PatternCheckingActivity : BaseActivity() {

    private var password: String? = null
    private var patternPassword: String? = null
    private val patternHelper = PatternHelper()

    private lateinit var walletRelease: WalletRelease

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pattern_checking)
        StatusBarHelper.setStatusBarLightMode(this)

        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(false)

        leftTitle.setOnClickListener {
            finish()
        }

        val wr = intent.getParcelableExtra<WalletRelease>("walletRelease")
        if (wr == null) {
            finish()
            return
        }
        walletRelease = wr

        decryptedPattern()

        patternLockerView.normalCellView = NormalCellView().setRadius(dp2px(8f))
        patternLockerView.hitCellView = RippleLockerHitCellView()

        patternLockerView.setOnPatternChangedListener(object : OnPatternChangeListener {
            override fun onStart(view: PatternLockerView) {}

            override fun onChange(view: PatternLockerView, hitIndexList: List<Int>) {
            }

            override fun onComplete(view: PatternLockerView, hitIndexList: List<Int>) {
                val isError = !isPatternOk(hitIndexList)
                view.updateStatus(isError)
                updateMsg()
            }

            override fun onClear(view: PatternLockerView) {
            }
        })

        usePassword.setOnClickListener {
            showPasswordDialog()
        }
    }

    private fun isPatternOk(hitIndexList: List<Int>): Boolean {
        patternHelper.validateForChecking(hitIndexList, patternPassword)
        if (patternHelper.isFinish) {
            if (patternHelper.isOk) {
                finishPattern(password)
            } else {
                showPasswordDialog()
            }
        }
        return patternHelper.isOk
    }

    private fun updateMsg() {
        when (patternHelper.message) {
            PatternHelper.CODE_CHECKING_SUCCESS -> {
                msg.text = getString(R.string.pattern_success)
            }
            PatternHelper.CODE_SIZE_ERROR -> {
                msg.text = getString(R.string.pattern_canvas_failed, PatternHelper.MAX_SIZE.toString())
            }
            PatternHelper.CODE_PWD_ERROR -> {
                msg.text = getString(R.string.pattern_failed, patternHelper.remainTimes.toString())
            }
        }
        msg.setTextColor(
                if (patternHelper.isOk)
                    ContextCompat.getColor(this, R.color.color_333333)
                else
                    ContextCompat.getColor(this, R.color.color_FF3A5C)
        )
    }

    private fun showPasswordDialog() {
        PasswordDialog.display(supportFragmentManager, walletRelease.walletId) {
            finishPattern(it)
        }
    }

    private fun finishPattern(pwd: String?) {
        val intent = Intent()
        intent.putExtra("password", pwd)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun decryptedPattern() {
        try {
            val cipher = getRSACipher()
            cipher.init(Cipher.DECRYPT_MODE, getRSAKey(this).privateKey)
            val decryptedPassword = processRSAData(Base64.decode(walletRelease.password, Base64.URL_SAFE), cipher, false)
            val decryptedPattern = processRSAData(Base64.decode(walletRelease.patternPassword, Base64.URL_SAFE), cipher, false)
            password = String(decryptedPassword)
            patternPassword = String(decryptedPattern)
        } catch (e: Exception) {
            e.printStackTrace()
            toast(R.string.data_exception)
            finish()
        }
    }
}
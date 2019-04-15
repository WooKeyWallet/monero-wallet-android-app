package io.wookey.wallet.feature.generate

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.feature.generate.create.BackupMnemonicActivity
import io.wookey.wallet.feature.generate.recovery.RecoveryWalletActivity
import io.wookey.wallet.feature.setting.WebViewActivity
import io.wookey.wallet.support.BackgroundHelper
import io.wookey.wallet.support.LengthFilter
import io.wookey.wallet.support.extensions.afterTextChanged
import io.wookey.wallet.support.extensions.clickableSpan
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.extensions.toast
import kotlinx.android.synthetic.main.activity_generate_wallet.*

class GenerateWalletActivity : BaseTitleSecondActivity() {

    private lateinit var viewModel: GenerateWalletViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_wallet)

        viewModel = ViewModelProviders.of(this).get(GenerateWalletViewModel::class.java)

        viewModel.title.observe(this, Observer { value ->
            value?.let {
                setCenterTitle(it)
            }
        })

        agree.buttonDrawable = BackgroundHelper.getCheckBoxButton(this)
        next.background = BackgroundHelper.getButtonBackground(this)

        val term = getString(R.string.agreement_term)
        val s = "${getString(R.string.agreement_prompt)} $term"
        val start = s.indexOf(term)
        val end = start + term.length
        val style = SpannableString(s)
        style.clickableSpan(start..end, ContextCompat.getColor(this, R.color.color_2179FF)) {
            startActivity(Intent(this, WebViewActivity::class.java))
        }
        agreement.text = style
        agreement.movementMethod = LinkMovementMethod.getInstance()

        dot1.setImageDrawable(BackgroundHelper.getDotDrawable(this, R.color.color_FFFFFF, dp2px(6)))
        dot2.setImageDrawable(BackgroundHelper.getDotDrawable(this, R.color.color_FFFFFF, dp2px(6)))

        walletName.editText?.filters = arrayOf(LengthFilter(20))

        walletName.editText?.afterTextChanged {
            viewModel.setWalletName(it)
        }
        setPassword.editText?.afterTextChanged {
            viewModel.setPassword(it)
        }
        confirmPassword.editText?.afterTextChanged {
            viewModel.setConfirmPassword(it)
        }
        switchPassword.setOnClickListener {
            viewModel.switchPassword()
        }
        passwordPrompt.editText?.afterTextChanged {
            viewModel.setPasswordPrompt(it)
        }
        agree.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAgree(isChecked)
        }

        next.setOnClickListener {
            viewModel.next()
        }

        viewModel.passwordVisible.observe(this, Observer {
            setPassword.editText?.transformationMethod = HideReturnsTransformationMethod.getInstance()
            confirmPassword.editText?.transformationMethod = HideReturnsTransformationMethod.getInstance()
            switchPassword.setImageResource(R.drawable.icon_password_show)
        })
        viewModel.passwordInvisible.observe(this, Observer {
            setPassword.editText?.transformationMethod = PasswordTransformationMethod.getInstance()
            confirmPassword.editText?.transformationMethod = PasswordTransformationMethod.getInstance()
            switchPassword.setImageResource(R.drawable.icon_password_hide)
        })
        viewModel.passwordStrength.observe(this, Observer { value ->
            value?.let {
                passwordGrade.setCurrentGrade(it)
            }
        })

        viewModel.enabled.observe(this, Observer { value ->
            value?.let {
                next.isEnabled = it
            }
        })

        viewModel.createWallet.observe(this, Observer { value -> value?.let { createWallet(it) } })

        viewModel.recoveryWallet.observe(this, Observer { value -> value?.let { recoveryWallet(it) } })

        viewModel.showLoading.observe(this, Observer { showLoading() })
        viewModel.hideLoading.observe(this, Observer { hideLoading() })
        viewModel.toast.observe(this, Observer { toast(it) })

        viewModel.walletNameError.observe(this, Observer {
            if (it != null && it) {
                walletName.error = getString(R.string.wallet_invalid)
            } else {
                walletName.error = null
            }
        })

        viewModel.passwordError.observe(this, Observer {
            if (it != null && it) {
                setPassword.error = getString(R.string.password_invalid)
            } else {
                setPassword.error = null
            }
        })

        viewModel.confirmPasswordError.observe(this, Observer {
            if (it != null && it) {
                confirmPassword.error = getString(R.string.confirm_password_invalid)
            } else {
                confirmPassword.error = null
            }
        })
    }

    private fun createWallet(intent: Intent) {
        startActivity(intent.apply { setClass(this@GenerateWalletActivity, BackupMnemonicActivity::class.java) })
    }

    private fun recoveryWallet(intent: Intent) {
        startActivity(intent.apply { setClass(this@GenerateWalletActivity, RecoveryWalletActivity::class.java) })
    }
}

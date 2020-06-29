package io.wookey.wallet.feature.wallet

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.data.entity.WalletRelease
import io.wookey.wallet.dialog.PromptDialog
import io.wookey.wallet.feature.auth.PatternSettingActivity
import io.wookey.wallet.support.REQUEST_PATTERN_SETTING
import io.wookey.wallet.support.extensions.*
import kotlinx.android.synthetic.main.activity_release_mode.*
import javax.crypto.Cipher

class ReleaseModeActivity : BaseTitleSecondActivity() {

    private lateinit var viewModel: ReleaseModeViewModel
    private var switchChecked1 = false
    private var switchChecked2 = false
    private lateinit var walletRelease: WalletRelease
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_release_mode)

        setCenterTitle(R.string.release_mode)
        setRightIcon(R.drawable.icon_help)
        setRightIconClick(View.OnClickListener {
            PromptDialog.display(
                    supportFragmentManager,
                    getString(R.string.dialog_title_release_risk),
                    getString(R.string.release_prompt_open),
                    getString(R.string.dialog_confirm_know)
            ) {}
        })

        val pwd = intent.getStringExtra("password")
        val walletId = intent.getIntExtra("walletId", -1)
        if (pwd.isNullOrBlank() || walletId < 0) {
            finish()
            return
        }

        password = pwd

        viewModel = ViewModelProviders.of(this).get(ReleaseModeViewModel::class.java)

        viewModel.loadData(walletId)

        viewModel.walletRelease.observe(this, Observer {
            switchChecked1 = it?.fingerprint ?: false
            switchChecked2 = it?.pattern ?: false
            if (it == null) {
                walletRelease = WalletRelease(walletId = walletId, openWallet = true)
                setCheckbox(walletRelease)
                switch1.isChecked = false
                switch2.isChecked = false
            } else {
                walletRelease = it
                setCheckbox(walletRelease)
                switch1.isChecked = it.fingerprint
                switch2.isChecked = it.pattern
            }
            setPatternModifyVisibility()
        })

        group1.setOnClickListener {
            if (::walletRelease.isInitialized) {
                if (walletRelease.openWallet && !walletRelease.sendTransaction && !walletRelease.backup) {
                    toast(R.string.release_mode_least)
                    return@setOnClickListener
                }
                walletRelease.openWallet = !walletRelease.openWallet
                setCheckbox1(walletRelease)
                viewModel.insertOrUpdateData(walletRelease)
            }
        }

        group2.setOnClickListener {
            if (::walletRelease.isInitialized) {
                if (!walletRelease.openWallet && walletRelease.sendTransaction && !walletRelease.backup) {
                    toast(R.string.release_mode_least)
                    return@setOnClickListener
                }
                walletRelease.sendTransaction = !walletRelease.sendTransaction
                setCheckbox2(walletRelease)
                viewModel.insertOrUpdateData(walletRelease)
            }
        }

        group3.setOnClickListener {
            if (::walletRelease.isInitialized) {
                if (!walletRelease.openWallet && !walletRelease.sendTransaction && walletRelease.backup) {
                    toast(R.string.release_mode_least)
                    return@setOnClickListener
                }
                walletRelease.backup = !walletRelease.backup
                setCheckbox3(walletRelease)
                viewModel.insertOrUpdateData(walletRelease)
            }
        }

        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                modeGroup1.visibility = View.VISIBLE
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                modeGroup1.visibility = View.GONE
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                modeGroup1.visibility = View.GONE
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                modeGroup1.visibility = View.VISIBLE
            }
        }
        switch1.setOnCheckedChangeListener { buttonView, isChecked ->
            if (switchChecked1 == isChecked) {
                return@setOnCheckedChangeListener
            }
            if (isChecked && biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                PromptDialog.display(
                        supportFragmentManager,
                        getString(R.string.dialog_title_prompt),
                        getString(R.string.biometric_not_set),
                        getString(R.string.dialog_confirm)
                ) {}
                switchChecked1 = false
                switch1.isChecked = false
                return@setOnCheckedChangeListener
            }
            if (isChecked && switch2.isChecked) {
                PromptDialog.display(
                        supportFragmentManager,
                        getString(R.string.dialog_title_prompt),
                        getString(R.string.release_prompt_incompatible),
                        getString(R.string.dialog_confirm)
                ) {}
                switchChecked1 = false
                switch1.isChecked = false
                return@setOnCheckedChangeListener
            }
            if (isChecked) {
                PromptDialog.display(
                        supportFragmentManager,
                        getString(R.string.dialog_title_release_risk),
                        getString(R.string.release_prompt_open),
                        getString(R.string.dialog_confirm_know)
                ) {
                    switchChecked1 = true
                    setBiometric(password)
                }
            } else {
                PromptDialog.display(
                        supportFragmentManager,
                        getString(R.string.dialog_title_prompt),
                        getString(R.string.release_prompt_close),
                        getString(R.string.dialog_confirm)
                ) {
                    switchChecked1 = false
                    viewModel.deleteData(walletRelease)
                }
            }
        }

        switch2.setOnCheckedChangeListener { buttonView, isChecked ->
            if (switchChecked2 == isChecked) {
                return@setOnCheckedChangeListener
            }
            if (isChecked && switch1.isChecked) {
                PromptDialog.display(
                        supportFragmentManager,
                        getString(R.string.dialog_title_prompt),
                        getString(R.string.release_prompt_incompatible),
                        getString(R.string.dialog_confirm)
                ) {}
                switchChecked2 = false
                switch2.isChecked = false
                setPatternModifyVisibility()
                return@setOnCheckedChangeListener
            }
            if (isChecked) {
                PromptDialog.display(
                        supportFragmentManager,
                        getString(R.string.dialog_title_release_risk),
                        getString(R.string.release_prompt_open),
                        getString(R.string.dialog_confirm_know)
                ) {
                    switchChecked2 = true
                    setPatternModifyVisibility()
                    startActivityForResult(Intent(this, PatternSettingActivity::class.java), REQUEST_PATTERN_SETTING)
                }
            } else {
                PromptDialog.display(
                        supportFragmentManager,
                        getString(R.string.dialog_title_prompt),
                        getString(R.string.release_prompt_close),
                        getString(R.string.dialog_confirm)
                ) {
                    switchChecked2 = false
                    setPatternModifyVisibility()
                    viewModel.deleteData(walletRelease)
                }
            }
        }

        modify2.setOnClickListener {
            startActivityForResult(Intent(this, PatternSettingActivity::class.java), REQUEST_PATTERN_SETTING)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            recoveryPatternState()
            return
        }
        when (requestCode) {
            REQUEST_PATTERN_SETTING -> {
                if (data != null) {
                    val pattern = data.getStringExtra("pattern")
                    if (!pattern.isNullOrBlank()) {
                        encryptedPattern(pattern)
                        viewModel.insertOrUpdateData(walletRelease)
                        toast(R.string.pattern_open_success)
                    } else {
                        recoveryPatternState()
                    }
                } else {
                    recoveryPatternState()
                }
            }
        }
    }

    private fun recoveryPatternState() {
        if (walletRelease.pattern) {
            switchChecked2 = true
            switch2.isChecked = true
            setPatternModifyVisibility()
        } else {
            switchChecked2 = false
            switch2.isChecked = false
            setPatternModifyVisibility()
        }
    }

    private fun setPatternModifyVisibility() {
        if (switch2.isChecked) {
            divider5.visibility = View.INVISIBLE
            modifyGroup2.visibility = View.VISIBLE
        } else {
            divider5.visibility = View.VISIBLE
            modifyGroup2.visibility = View.GONE
        }
    }

    private fun encryptedPattern(pattern: String) {
        try {
            val cipher = getRSACipher()
            cipher.init(Cipher.ENCRYPT_MODE, getRSAKey(this).certificate.publicKey)
            val encryptedPassword = processRSAData(password.toByteArray(), cipher, true)
            val encryptedPattern = processRSAData(pattern.toByteArray(), cipher, true)
            walletRelease.pattern = true
            walletRelease.password = Base64.encodeToString(encryptedPassword, Base64.URL_SAFE)
            walletRelease.patternPassword = Base64.encodeToString(encryptedPattern, Base64.URL_SAFE)
            walletRelease.iv = ""
        } catch (e: Exception) {
            e.printStackTrace()
            walletRelease.pattern = false
            walletRelease.password = ""
            walletRelease.patternPassword = ""
            walletRelease.iv = ""
            switchChecked2 = false
            switch2.isChecked = false
            setPatternModifyVisibility()
        }
    }

    private fun setCheckbox(it: WalletRelease) {
        setCheckbox1(it)
        setCheckbox2(it)
        setCheckbox3(it)
    }

    private fun setCheckbox1(it: WalletRelease) {
        if (it.openWallet) {
            Glide.with(this).asGif().skipMemoryCache(true).load(R.drawable.gif_open_wallet).into(image1)
            checkbox1.setImageResource(R.drawable.icon_round_checked)
        } else {
            image1.setImageResource(R.drawable.bg_open_wallet)
            checkbox1.setImageResource(R.drawable.icon_round_unchecked)
        }
    }

    private fun setCheckbox2(it: WalletRelease) {
        if (it.sendTransaction) {
            Glide.with(this).asGif().skipMemoryCache(true).load(R.drawable.gif_send_coin).into(image2)
            checkbox2.setImageResource(R.drawable.icon_round_checked)
        } else {
            image2.setImageResource(R.drawable.bg_send_coin)
            checkbox2.setImageResource(R.drawable.icon_round_unchecked)
        }
    }

    private fun setCheckbox3(it: WalletRelease) {
        if (it.backup) {
            Glide.with(this).asGif().skipMemoryCache(true).load(R.drawable.gif_backup).into(image3)
            checkbox3.setImageResource(R.drawable.icon_round_checked)
        } else {
            image3.setImageResource(R.drawable.bg_backup)
            checkbox3.setImageResource(R.drawable.icon_round_unchecked)
        }
    }

    private fun setBiometric(password: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val biometricPrompt = BiometricPrompt(this,
                    ContextCompat.getMainExecutor(this),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            switchChecked1 = false
                            switch1.isChecked = false
                            toast(R.string.biometric_open_failed)
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            switchChecked1 = true
                            switch1.isChecked = true
                            val cipher = result.cryptoObject?.cipher
                            val encryptedInfo = cipher?.doFinal(password.toByteArray())
                            walletRelease.fingerprint = true
                            walletRelease.password =
                                    Base64.encodeToString(encryptedInfo, Base64.URL_SAFE)
                            walletRelease.iv = Base64.encodeToString(cipher?.iv, Base64.URL_SAFE)
                            viewModel.insertOrUpdateData(walletRelease)
                            toast(R.string.biometric_open_success)
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                        }
                    })
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.biometric_prompt_title))
                    .setSubtitle(getString(R.string.biometric_prompt_subtitle1))
                    .setNegativeButtonText(getString(R.string.cancel))
                    .build()
            val cipher = getCipher()
            val secretKey = getSecretKey()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }
}

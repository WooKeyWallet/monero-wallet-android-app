package io.wookey.wallet.feature.auth

import android.content.Intent
import android.os.Build
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseActivity
import io.wookey.wallet.data.entity.WalletRelease
import io.wookey.wallet.dialog.PasswordDialog
import io.wookey.wallet.support.REQUEST_PATTERN_CHECKING
import io.wookey.wallet.support.extensions.getCipher
import io.wookey.wallet.support.extensions.getSecretKey
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

class AuthManager(val walletRelease: WalletRelease?, val walletId: Int) {

    fun openWallet(activity: BaseActivity, fragment: Fragment? = null, requestCode: Int = REQUEST_PATTERN_CHECKING, block: (String?) -> Unit) {
        val fragmentManager = fragment?.childFragmentManager ?: activity.supportFragmentManager
        if (walletRelease == null) {
            showPasswordDialog(fragmentManager, block)
            return
        }
        if (walletRelease.openWallet) {
            when {
                walletRelease.fingerprint -> {
                    if (BiometricManager.from(activity).canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS) {
                        showPasswordDialog(fragmentManager, block)
                    } else {
                        showBiometricPrompt(activity, fragmentManager, block)
                    }
                }
                walletRelease.pattern -> {
                    showPattern(activity, fragment, requestCode, block)
                }
                else -> {
                    showPasswordDialog(fragmentManager, block)
                }
            }
        } else {
            showPasswordDialog(fragmentManager, block)
        }
    }

    fun sendTransaction(activity: BaseActivity, fragment: Fragment? = null, requestCode: Int = REQUEST_PATTERN_CHECKING, block: (String?) -> Unit) {
        val fragmentManager = fragment?.childFragmentManager ?: activity.supportFragmentManager
        if (walletRelease == null) {
            showPasswordDialog(fragmentManager, block)
            return
        }
        if (walletRelease.sendTransaction) {
            when {
                walletRelease.fingerprint -> {
                    if (BiometricManager.from(activity).canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS) {
                        showPasswordDialog(fragmentManager, block)
                    } else {
                        showBiometricPrompt(activity, fragmentManager, block)
                    }
                }
                walletRelease.pattern -> {
                    showPattern(activity, fragment, requestCode, block)
                }
                else -> {
                    showPasswordDialog(fragmentManager, block)
                }
            }
        } else {
            showPasswordDialog(fragmentManager, block)
        }
    }

    fun backup(activity: BaseActivity, fragment: Fragment? = null, requestCode: Int = REQUEST_PATTERN_CHECKING, block: (String?) -> Unit) {
        val fragmentManager = fragment?.childFragmentManager ?: activity.supportFragmentManager
        if (walletRelease == null) {
            showPasswordDialog(fragmentManager, block)
            return
        }
        if (walletRelease.backup) {
            when {
                walletRelease.fingerprint -> {
                    if (BiometricManager.from(activity).canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS) {
                        showPasswordDialog(fragmentManager, block)
                    } else {
                        showBiometricPrompt(activity, fragmentManager, block)
                    }
                }
                walletRelease.pattern -> {
                    showPattern(activity, fragment, requestCode, block)
                }
                else -> {
                    showPasswordDialog(fragmentManager, block)
                }
            }
        } else {
            showPasswordDialog(fragmentManager, block)
        }
    }

    private fun showPasswordDialog(fragmentManager: FragmentManager, block: (String?) -> Unit) {
        PasswordDialog.display(fragmentManager, walletId) { password ->
            block(password)
        }
    }

    private fun showBiometricPrompt(activity: BaseActivity, fragmentManager: FragmentManager, block: (String?) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val biometricPrompt = BiometricPrompt(activity,
                    ContextCompat.getMainExecutor(activity), object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showPasswordDialog(fragmentManager, block)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val decryptedInfo = result.cryptoObject?.cipher?.doFinal(Base64.decode(walletRelease?.password, Base64.URL_SAFE))
                    decryptedInfo?.let {
                        block(String(it))
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(activity.getString(R.string.biometric_prompt_title))
                    .setSubtitle(activity.getString(R.string.biometric_prompt_subtitle1))
                    .setNegativeButtonText(activity.getString(R.string.use_password))
                    .build()
            val cipher = getCipher()
            val secretKey = getSecretKey()
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(Base64.decode(walletRelease?.iv, Base64.URL_SAFE)))
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun showPattern(activity: BaseActivity, fragment: Fragment?, requestCode: Int, block: (String?) -> Unit) {
        if (fragment == null) {
            activity.startActivityForResult(Intent(activity, PatternCheckingActivity::class.java).apply {
                putExtra("walletRelease", walletRelease)
            }, requestCode)
        } else {
            fragment.startActivityForResult(Intent(activity, PatternCheckingActivity::class.java).apply {
                putExtra("walletRelease", walletRelease)
            }, requestCode)
        }
        block(null)
    }
}
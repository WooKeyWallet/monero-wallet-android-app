package io.wookey.wallet.feature.generate

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.core.XMRRepository
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.support.WALLET_CREATE
import io.wookey.wallet.support.WALLET_RECOVERY
import io.wookey.wallet.support.extensions.putInt
import io.wookey.wallet.support.extensions.sharedPreferences
import io.wookey.wallet.support.viewmodel.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GenerateWalletViewModel : BaseViewModel() {

    val title = MutableLiveData<Int>()
    val type = sharedPreferences().getInt("type", WALLET_CREATE)

    val walletName = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val passwordStrength = MutableLiveData<Int>()
    val confirmPassword = MutableLiveData<String>()
    val passwordPrompt = MutableLiveData<String>()
    val agree = MutableLiveData<Boolean>()
    val enabled = MediatorLiveData<Boolean>()

    val passwordVisible = SingleLiveEvent<Unit>()
    val passwordInvisible = SingleLiveEvent<Unit>()

    val createWallet = MutableLiveData<Intent>()
    val recoveryWallet = MutableLiveData<Intent>()

    private val repository = XMRRepository()

    private var passwordVisibility = false

    val showLoading = MutableLiveData<Boolean>()
    val hideLoading = MutableLiveData<Boolean>()
    val toast = MutableLiveData<String>()

    val walletNameError = MutableLiveData<Boolean>()
    val passwordError = MutableLiveData<Boolean>()
    val confirmPasswordError = MutableLiveData<Boolean>()

    var walletValidJob: Job? = null

    init {
        setType(type)
        enabled.addSource(walletName) {
            enabled.value = checkValid()
        }
        enabled.addSource(password) {
            enabled.value = checkValid()
        }
        enabled.addSource(confirmPassword) {
            enabled.value = checkValid()
        }
        enabled.addSource(agree) {
            enabled.value = checkValid()
        }
    }

    private fun setType(value: Int) {
        when (value) {
            WALLET_CREATE -> title.value = R.string.create_wallet
            WALLET_RECOVERY -> title.value = R.string.recovery_wallet
        }
    }

    fun setWalletName(it: String) {
        walletValidJob?.cancel()
        if (it.isNullOrBlank()) {
            walletNameError.value = null
            walletName.value = it
        } else {
            walletValidJob = uiScope.launch {
                withContext(Dispatchers.IO) {
                    val symbol = sharedPreferences().getString("symbol", "XMR") ?: "XMR"
                    val count = AppDatabase.getInstance().walletDao().countWalletsByName(symbol, it)
                    if (count > 0) {
                        walletNameError.postValue(true)
                    } else {
                        walletNameError.postValue(false)
                    }
                    walletName.postValue(it)
                }
            }
        }
    }

    fun setPassword(it: String) {
        if (it.isNullOrBlank()) {
            passwordError.value = null
        } else {
            passwordError.value = it.length < 8
            if (confirmPassword.value.isNullOrBlank()) {
                confirmPasswordError.value = null
            } else {
                confirmPasswordError.value = confirmPassword.value != it
            }
        }
        password.value = it
        calculatePasswordStrength(it)
    }

    private fun calculatePasswordStrength(it: String) {
        var value = 0
        if (it.isNullOrBlank() || it.length < 8) {
            value = 0
        } else {
            value = 25
            var upper = 0
            var lower = 0
            var number = 0
            var other = 0
            it.forEach { char ->
                when {
                    Character.isUpperCase(char) -> upper++
                    Character.isLowerCase(char) -> lower++
                    char in '0'..'9' -> number++
                    else -> other++
                }
            }
            if (upper == it.length || lower == it.length) {
                value += 10
            }
            if (upper > 0 || lower > 0) {
                value += 30
            }
            if (number == 1) {
                value += 10
            }
            if (number >= 3) {
                value += 20
            }
            if (other == 1) {
                value += 10
            }
            if (other >= 2) {
                value += 25
            }
            if (number > 0 && (upper > 0 || lower > 0) && other == 0) {
                value += 2
            }
            if (other > 0 && (upper > 0 || lower > 0) && number == 0) {
                value += 2
            }
            if (number > 0 && other > 0) {
                value += if (upper > 0 && lower > 0) 5 else 3
            }
        }
        passwordStrength.value = when {
            value >= 90 -> 4
            value >= 70 -> 3
            value >= 47 -> 2
            value >= 25 -> 1
            else -> 0
        }
    }

    fun setConfirmPassword(it: String) {
        if (it.isNullOrBlank()) {
            confirmPasswordError.value = null
        } else {
            confirmPasswordError.value = password.value != it
        }
        confirmPassword.value = it
    }

    fun setPasswordPrompt(it: String) {
        passwordPrompt.value = it
    }

    fun setAgree(it: Boolean) {
        agree.value = it
    }

    private fun checkValid(): Boolean {
        if (walletName.value.isNullOrBlank()) {
            return false
        }
        val error1 = walletNameError.value
        if (error1 != null && error1) {
            return false
        }
        if (password.value.isNullOrBlank()) {
            return false
        }
        val error2 = passwordError.value
        if (error2 != null && error2) {
            return false
        }
        if (confirmPassword.value.isNullOrBlank()) {
            return false
        }
        val error3 = confirmPasswordError.value
        if (error3 != null && error3) {
            return false
        }
        if (agree.value == null || !agree.value!!) {
            return false
        }
        return true
    }

    fun next() {
        when (type) {
            WALLET_CREATE -> {
                create()
            }
            WALLET_RECOVERY -> {
                recovery()
            }
        }
    }

    private fun create() {
        enabled.value = false
        showLoading.postValue(true)
        val name = walletName.value!!
        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val wallet = repository.createWallet(name, password.value!!)
                    wallet!!.passwordPrompt = passwordPrompt.value ?: ""
                    val insert = repository.saveWallet(wallet)
                    sharedPreferences().putInt("walletId", insert!!.id)
                    hideLoading.postValue(true)
                    createWallet.postValue(Intent().apply {
                        putExtra("walletId", insert.id)
                        putExtra("seed", wallet.seed)
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                    repository.deleteWallet(name)
                    hideLoading.postValue(true)
                    toast.postValue(e.message)
                } finally {
                    enabled.postValue(true)
                }
            }
        }
    }

    private fun recovery() {
        recoveryWallet.postValue(Intent().apply {
            putExtra("walletName", walletName.value)
            putExtra("password", password.value)
            putExtra("passwordPrompt", passwordPrompt.value)
        })
    }

    fun switchPassword() {
        passwordVisibility = !passwordVisibility
        if (passwordVisibility) {
            passwordVisible.call()
        } else {
            passwordInvisible.call()
        }
    }
}
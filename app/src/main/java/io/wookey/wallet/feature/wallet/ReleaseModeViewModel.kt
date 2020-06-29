package io.wookey.wallet.feature.wallet

import android.os.Build
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.WalletRelease
import io.wookey.wallet.support.extensions.getCipher
import io.wookey.wallet.support.extensions.getSecretKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.crypto.Cipher

class ReleaseModeViewModel : BaseViewModel() {

    val walletRelease = MutableLiveData<WalletRelease>()

    fun loadData(walletId: Int) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                walletRelease.postValue(AppDatabase.getInstance().walletReleaseDao().loadDataByWalletId(walletId))
            }
        }
    }

    fun insertOrUpdateData(wr: WalletRelease) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val release = AppDatabase.getInstance().walletReleaseDao().loadDataByWalletId(wr.walletId)
                if (release == null) {
                    AppDatabase.getInstance().walletReleaseDao().insert(wr)
                } else {
                    AppDatabase.getInstance().walletReleaseDao().update(wr.apply { id = release.id })
                }
            }
        }
    }

    fun deleteData(wr: WalletRelease) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                AppDatabase.getInstance().walletReleaseDao().delete(wr)
                walletRelease.postValue(null)
            }
        }
    }
}
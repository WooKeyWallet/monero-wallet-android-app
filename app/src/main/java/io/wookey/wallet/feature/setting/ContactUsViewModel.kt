package io.wookey.wallet.feature.setting

import android.arch.lifecycle.MutableLiveData
import io.wookey.wallet.base.BaseViewModel

class ContactUsViewModel : BaseViewModel() {

    var openBrowser = MutableLiveData<String>()
    var copyUrl = MutableLiveData<String>()

    fun openBrowser(url: String) {
        openBrowser.value = url
    }

    fun copyUrl(url: String) {
        copyUrl.value = url
    }
}
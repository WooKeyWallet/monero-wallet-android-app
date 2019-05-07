package io.wookey.wallet.feature.setting

import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.core.XMRRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NodeSettingViewModel : BaseViewModel() {

    private val repository = XMRRepository()

    init {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                repository.insertNodes()
            }
        }
    }
}
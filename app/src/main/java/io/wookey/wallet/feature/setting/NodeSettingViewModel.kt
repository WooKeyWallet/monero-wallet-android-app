package io.wookey.wallet.feature.setting

import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.support.nodeArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NodeSettingViewModel : BaseViewModel() {
    init {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                AppDatabase.getInstance().nodeDao().insertNodes(nodes = *nodeArray)
            }
        }
    }
}
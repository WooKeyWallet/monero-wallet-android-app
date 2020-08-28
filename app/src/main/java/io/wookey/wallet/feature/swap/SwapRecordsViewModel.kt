package io.wookey.wallet.feature.swap

import androidx.lifecycle.MutableLiveData
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.SwapRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SwapRecordsViewModel : BaseViewModel() {

    val records = MutableLiveData<List<SwapRecord>>()
    var offset = 0
    var loadFinish = false
    var loading = false

    fun loadData() {
        if (loading) {
            return
        }
        loading = true
        offset = 0
        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    records.postValue(
                        AppDatabase.getInstance().swapRecordDao().getSwapRecords(offset)
                    )
                    println(records.value)
                } catch (e: Exception) {
                    e.printStackTrace()
                    records.postValue(null)
                }
                loading = false
            }
        }
    }

    fun loadMore() {
        if (loading) {
            return
        }
        if (loadFinish) {
            return
        }
        loading = true
        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val list = mutableListOf<SwapRecord>()
                    list.addAll(records.value ?: emptyList())
                    offset = list.size
                    val swapRecords =
                        AppDatabase.getInstance().swapRecordDao().getSwapRecords(offset)
                    println(swapRecords)
                    if (swapRecords.isNullOrEmpty()) {
                        loadFinish = true
                    } else {
                        list.addAll(swapRecords)
                    }
                    records.postValue(list)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                loading = false
            }
        }
    }

}
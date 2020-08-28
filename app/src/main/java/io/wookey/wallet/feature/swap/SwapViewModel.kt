package io.wookey.wallet.feature.swap

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseViewModel
import io.wookey.wallet.core.XMRRepository
import io.wookey.wallet.core.XMRWalletController
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.*
import io.wookey.wallet.data.remote.RetrofitManager
import io.wookey.wallet.data.remote.entity.Empty
import io.wookey.wallet.data.remote.entity.ExchangeAmount
import io.wookey.wallet.data.remote.entity.RPCRequest
import io.wookey.wallet.data.remote.entity.SwapCreateTransaction
import io.wookey.wallet.support.extensions.unWrap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class SwapViewModel : BaseViewModel() {

    val showLoading = MutableLiveData<Boolean>()
    val hideLoading = MutableLiveData<Boolean>()
    val toast = MutableLiveData<String>()
    val toastRes = MutableLiveData<Int>()

    val walletRelease = MutableLiveData<WalletRelease>()

    val enabled = MutableLiveData<Boolean>()

    val currencies = MutableLiveData<List<String>>()
    val toCurrency = MutableLiveData<String>()
    val minFromAmount = MutableLiveData<String>()
    val exchangeAmount = MutableLiveData<ExchangeAmount>()

    val balance = MutableLiveData<String>()
    val estimatedAmount = MutableLiveData<String>()

    private var toCoin = ""
    private var fromAmount = ""
    private var toAddress = ""
    private var enableAddressTag = false
    private var toAddressTag = ""
    private var extraID = ""
    private var requireExtraID = false
    private var refundAddress = ""
    private var walletId = -1

    private var swapCreateTransaction: SwapCreateTransaction? = null

    private val repository = XMRRepository()
    val connecting = MutableLiveData<Int>()
    val synchronizing = MutableLiveData<Long>()
    val synchronized = MutableLiveData<Int>()
    var send = true
        private set
    val sendStatus = MutableLiveData<Int>()
    var walletResume = AtomicBoolean(false)

    var loading = false

    private var observer = object : XMRWalletController.Observer {
        var firstBlock = 0L

        override fun onWalletOpened() {
        }

        override fun onWalletOpenFailed(error: String?) {
            failed(R.string.block_synchronize_failed)
        }

        override fun onWalletStarted() {}

        override fun onWalletStartFailed(error: String?) {
            failed(R.string.block_synchronize_failed)
        }

        override fun onRefreshed(height: Long?) {
            firstBlock = refresh(firstBlock)
        }
    }

    fun loadData() {
        if (loading) {
            return
        }
        loading = true
        showLoading.value = true
        uiScope.launch {
            try {
                val list = mutableListOf<String>()
                val unWrap = RetrofitManager.changellyService.getCurrencies(
                    RPCRequest(
                        method = "getCurrencies",
                        params = Empty()
                    )
                ).unWrap()
                if (unWrap.isNullOrEmpty()) throw IllegalStateException("data exception")

                list.addAll(unWrap)
                list.remove("xmr")
                list.sortBy { it }

                val toCoin = if (list.contains("btc")) "btc" else list[0]
                setToCoin(toCoin)
                toCurrency.postValue(toCoin)

                currencies.postValue(list)

                minFromAmount.postValue(
                    RetrofitManager.changellyService.getMinAmount(
                        RPCRequest(
                            method = "getMinAmount",
                            params = mapOf("from" to "xmr", "to" to toCoin)
                        )
                    ).unWrap()
                )

                exchangeAmount.postValue(
                    RetrofitManager.changellyService.getExchangeAmount(
                        RPCRequest(
                            method = "getExchangeAmount",
                            params = listOf(mapOf("from" to "xmr", "to" to toCoin, "amount" to "1"))
                        )
                    ).unWrap()?.firstOrNull()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                toast.postValue(e.message)
            }
            hideLoading.postValue(true)
            loading = false
        }
    }

    fun switchToCoin(toCoin: String) {
        if (loading) {
            return
        }
        loading = true
        setToCoin(toCoin)
        toCurrency.value = toCoin
        showLoading.value = true
        uiScope.launch {
            try {
                minFromAmount.postValue(
                    RetrofitManager.changellyService.getMinAmount(
                        RPCRequest(
                            method = "getMinAmount",
                            params = mapOf("from" to "xmr", "to" to toCoin)
                        )
                    ).unWrap()
                )
                exchangeAmount.postValue(
                    RetrofitManager.changellyService.getExchangeAmount(
                        RPCRequest(
                            method = "getExchangeAmount",
                            params = listOf(mapOf("from" to "xmr", "to" to toCoin, "amount" to "1"))
                        )
                    ).unWrap()?.firstOrNull()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                toast.postValue(e.message)
            }
            hideLoading.postValue(true)
            loading = false
        }
    }

    fun getWalletRelease(id: Int) {
        uiScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    walletRelease.postValue(
                        AppDatabase.getInstance().walletReleaseDao().loadDataByWalletId(id)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                walletRelease.postValue(null)
            }
        }
    }

    fun confirm(password: String, walletId: Int) {
        if (!send) {
            return
        }
        send = false
        sendStatus.value = 0
        this.walletId = walletId
        walletResume.set(false)
        uiScope.launch {
            try {
                // 获取地址
                val map = mutableMapOf<String, String>()
                map["from"] = "xmr"
                map["to"] = toCoin
                map["address"] = toAddress
                if (extraID.isNotBlank()) {
                    map["extraId"] = extraID
                }
                if (refundAddress.isNotBlank()) {
                    map["refundAddress"] = refundAddress
                }
                if (fromAmount.isNotBlank()) {
                    map["amount"] = fromAmount
                }
                swapCreateTransaction = RetrofitManager.changellyService.createTransaction(
                    RPCRequest(
                        method = "createTransaction",
                        params = map
                    )
                ).unWrap()
                if (swapCreateTransaction == null) {
                    throw IllegalStateException("transaction create failed")
                }
                // 打开并同步钱包
                withContext(Dispatchers.IO) {
                    try {
                        if (enableAddressTag) {
                            saveSwapAddress()
                        }
//                        saveTransaction()
                        connecting.postValue(R.string.block_connecting)
                        val wallet = AppDatabase.getInstance().walletDao().getWalletById(walletId)
                            ?: throw IllegalStateException()
                        repository.insertNodes()
                        val node = AppDatabase.getInstance().nodeDao().getSymbolNode(wallet.symbol)
                            ?: throw IllegalStateException()
                        val split = node.url.split(":")
                        XMRWalletController.setNode(split[0], split[1].toInt())
                        val path = repository.getWalletFilePath(wallet.name)
                        observer.firstBlock = 0L
                        XMRWalletController.startWallet(
                            path,
                            password,
                            wallet.restoreHeight,
                            observer
                        )
                        walletResume.set(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        toast.postValue(e.message)
                        sendStatus.postValue(-1)
                        send = true
                        walletResume.set(false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                toast.postValue(e.message)
                sendStatus.postValue(-1)
                send = true
                walletResume.set(false)
            }
        }
    }

    private fun saveSwapAddress() {
        AppDatabase.getInstance().swapAddressBookDao()
            .insert(
                SwapAddressBook(
                    symbol = toCoin.toLowerCase(Locale.CHINA),
                    address = toAddress,
                    notes = toAddressTag
                )
            )
    }

    /**
     * 处理钱包异常情况，如打开失败，切换钱包等
     * @param stringId 异常信息
     */
    fun failed(stringId: Int) {
        walletResume.set(false)
        toastRes.postValue(stringId)
        sendStatus.postValue(-1)
        send = true
    }

    @Synchronized
    private fun refresh(firstBlock: Long): Long {
        var firstBlockHeight = firstBlock
        if (XMRWalletController.isSynchronized()) {
            // 更新余额
            updateBalance()
            // 发起交易
            sendTransaction()
        } else {
            // calculate progress
            val daemonHeight = XMRWalletController.getDaemonBlockChainHeight()
            val blockChainHeight = XMRWalletController.getBlockChainHeight()
            val n = daemonHeight - blockChainHeight
            Log.d(
                "refresh",
                "daemonHeight: $daemonHeight, blockChainHeight: $blockChainHeight, n: $n, " +
                        "daemonBlockChainTargetHeight: ${XMRWalletController.getDaemonBlockChainTargetHeight()}"
            )
            when {
                n > 0L -> {
                    if (firstBlockHeight == 0L) {
                        firstBlockHeight = blockChainHeight
                    }
                    synchronizing.postValue(n)
                }
                n == 0L -> {
                    synchronized.postValue(R.string.swap_block_synchronized)
                }
                else -> {
                    failed(R.string.swap_block_synchronize_failed)
                    XMRWalletController.stopWallet()
                }
            }
        }
        return firstBlockHeight
    }

    @Synchronized
    private fun updateBalance() {
        try {
            val b = XMRWalletController.getBalance()?.let {
                XMRWalletController.getDisplayAmount(it)
            }
            balance.postValue(b)
            AppDatabase.getInstance().walletDao().getWalletById(walletId)?.let {
                AppDatabase.getInstance().walletDao().updateWallets(it.apply { balance = b ?: "0" })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun sendTransaction() {
        if (!send) {
            try {
                swapCreateTransaction?.let {
                    XMRWalletController.createTransaction(
                        false,
                        it.payinAddress,
                        null,
                        it.amountExpectedFrom
                    )
                    XMRWalletController.sendTransaction()
                    // 保存changelly交易信息
                    saveTransaction()
                    sendStatus.postValue(1)
                } ?: sendStatus.postValue(-1)
            } catch (e: Exception) {
                e.printStackTrace()
                toast.postValue(e.message)
                sendStatus.postValue(-1)
            }
            send = true
            walletResume.set(false)
        }
    }

    @Synchronized
    private fun saveTransaction() {
        swapCreateTransaction?.let {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'", Locale.CHINA)
            simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val time = simpleDateFormat.parse(it.createdAt).time / 1000
            val swapRecord = SwapRecord(
                swapId = it.id,
                amountFrom = it.amountExpectedFrom,
                currencyFrom = it.currencyFrom,
                amountTo = it.amountExpectedTo ?: "",
                currencyTo = it.currencyTo,
                createdAt = time.toString()
            )
            AppDatabase.getInstance().swapRecordDao().insert(swapRecord)
        }
    }

    private fun setToCoin(toCoin: String) {
        this.toCoin = toCoin
        requireExtraID = toCoin.equals("EOS", true)
    }

    fun setFromAmount(it: String) {
        fromAmount = it
        calculateExchangeAmount()
        checkValid()
    }

    fun calculateExchangeAmount() {
        val exchange = exchangeAmount.value?.result
        if (exchange.isNullOrBlank()) {
            estimatedAmount.value = null
            return
        }
        if (fromAmount.isBlank()) {
            estimatedAmount.value = null
            return
        }
        try {
            estimatedAmount.value =
                BigDecimal(fromAmount).times(BigDecimal(exchange)).stripTrailingZeros()
                    .toPlainString()
        } catch (e: Exception) {
            e.printStackTrace()
            estimatedAmount.value = null
        }
    }

    fun setToAddress(it: String) {
        toAddress = it
        checkValid()
    }

    fun setEnabledAddressTag(it: Boolean) {
        enableAddressTag = it
        checkValid()
    }

    fun setToAddressTag(it: String) {
        toAddressTag = it
        checkValid()
    }

    fun setExtraID(it: String) {
        extraID = it
        checkValid()
    }

    fun setRefundAddress(it: String) {
        refundAddress = it
        checkValid()
    }

    fun checkValid() {
        if (fromAmount.isBlank()) {
            enabled.value = false
            return
        }
        if (toCoin.isBlank()) {
            enabled.value = false
            return
        }
        if (toAddress.isBlank()) {
            enabled.value = false
            return
        }
        val min = minFromAmount.value ?: "0.0"
        if (BigDecimal(fromAmount) < BigDecimal(min)) {
            enabled.value = false
            return
        }
        if (enableAddressTag && toAddressTag.isBlank()) {
            enabled.value = false
            return
        }
        if (requireExtraID && extraID.isBlank()) {
            enabled.value = false
            return
        }
        enabled.value = true
    }
}
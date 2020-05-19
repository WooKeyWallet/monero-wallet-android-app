package io.wookey.wallet.core

import android.util.Log
import io.wookey.wallet.support.extensions.sharedPreferences

import io.wookey.monero.data.Node
import io.wookey.monero.data.TxData
import io.wookey.monero.model.*
import io.wookey.monero.util.RestoreHeight
import io.wookey.wallet.data.entity.SubAddress
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object XMRWalletController {

    const val TAG = "XMRWalletController"

    const val MNEMONIC_LANGUAGE = "English"

    interface Observer {
        fun onWalletOpened()

        fun onWalletOpenFailed(error: String?)

        fun onWalletStarted()

        fun onWalletStartFailed(error: String?)

        fun onRefreshed(height: Long?)
    }

    fun createWallet(aFile: File, password: String): io.wookey.wallet.data.entity.Wallet {
        val newWallet = WalletManager.getInstance()
                .createWallet(aFile, password, MNEMONIC_LANGUAGE)
        val success = newWallet.status == Wallet.Status.Status_Ok
        return close(success, newWallet)
    }

    fun recoveryWallet(
            aFile: File,
            password: String,
            mnemonic: String,
            restoreHeight: Long
    ): io.wookey.wallet.data.entity.Wallet {
        val newWallet = WalletManager.getInstance()
                .recoveryWallet(aFile, password, mnemonic, restoreHeight)
        val success = newWallet.status == Wallet.Status.Status_Ok
        return close(success, newWallet)
    }

    fun createWalletWithKeys(
            aFile: File,
            password: String,
            restoreHeight: Long,
            address: String,
            viewKey: String,
            spendKey: String
    ): io.wookey.wallet.data.entity.Wallet {
        val newWallet = WalletManager.getInstance()
                .createWalletWithKeys(aFile, password, MNEMONIC_LANGUAGE, restoreHeight, address, viewKey, spendKey)
        val success = newWallet.status == Wallet.Status.Status_Ok
        return close(success, newWallet)
    }

    private fun close(success: Boolean, newWallet: Wallet): io.wookey.wallet.data.entity.Wallet {
        try {
            if (!success) {
                throw RuntimeException(newWallet.errorString)
            } else {
                val sym = sharedPreferences().getString("symbol", "") ?: ""
                return io.wookey.wallet.data.entity.Wallet().apply {
                    symbol = sym
                    name = newWallet.name
                    address = newWallet.address
                    restoreHeight = newWallet.restoreHeight
                    seed = newWallet.seed
                }
            }
        } finally {
            newWallet.close()
        }
    }


    fun setNode(host: String, port: Int) {
        WalletManager.getInstance().setDaemon(Node().also {
            it.host = host
            it.rpcPort = port
        })
    }

    fun verifyWalletPasswordOnly(keyPath: String, password: String) =
            WalletManager.getInstance().verifyWalletPasswordOnly(keyPath, password)


    fun openWallet(path: String, password: String) {
        stopWallet()

        val wallet = WalletManager.getInstance().openWallet(path, password)
        if (wallet == null) {
            stopWallet()
            throw IllegalStateException("wallet opened failed")
        }

        if (wallet.status != Wallet.Status.Status_Ok) {
            val error = wallet.errorString
            stopWallet()
            throw IllegalStateException(error)
        }
    }

    fun getSeed(): String {
        val wallet = getWallet() ?: return ""
        wallet.seedLanguage = MNEMONIC_LANGUAGE
        return wallet.seed
    }

    fun getPublicViewKey(): String {
        val wallet = getWallet() ?: return ""
        return wallet.publicViewKey
    }

    fun getPublicSpendKey(): String {
        val wallet = getWallet() ?: return ""
        return wallet.publicSpendKey
    }

    fun getSecretViewKey(): String {
        val wallet = getWallet() ?: return ""
        return wallet.secretViewKey
    }

    fun getSecretSpendKey(): String {
        val wallet = getWallet() ?: return ""
        return wallet.secretSpendKey
    }

    fun startWallet(path: String, password: String, restoreHeight: Long, observer: Observer) {

        stopWallet()

        val wallet = WalletManager.getInstance().openWallet(path, password)
        if (wallet == null) {
            stopWallet()
            observer.onWalletOpenFailed("wallet opened failed")
            return
        }

        if (wallet.status != Wallet.Status.Status_Ok) {
            val error = wallet.errorString
            stopWallet()
            observer.onWalletOpenFailed(error)
            return
        }

        observer.onWalletOpened()

        if (startRefresh(wallet, restoreHeight, observer)) return
        observer.onWalletStarted()
    }

    fun startRefresh(wallet: Wallet, restoreHeight: Long, observer: Observer): Boolean {
        wallet.init(0)
        wallet.restoreHeight = restoreHeight

        Log.d(TAG, "Using daemon ${WalletManager.getInstance().daemonAddress}")

        if (wallet.connectionStatus != Wallet.ConnectionStatus.ConnectionStatus_Connected) {
            val error = wallet.errorString
            observer.onWalletStartFailed(error)
            return true
        }

        wallet.setListener(object : WalletListener {

            var lastBlockTime: Long = 0
            var synced = false

            override fun moneySpent(txId: String?, amount: Long) {
                Log.d(TAG, "moneySpent txId: $txId, amount: $amount")
            }

            override fun moneyReceived(txId: String?, amount: Long) {
                Log.d(TAG, "moneyReceived txId: $txId, amount: $amount")
            }

            override fun unconfirmedMoneyReceived(txId: String?, amount: Long) {
                Log.d(TAG, "unconfirmedMoneyReceived txId: $txId, amount: $amount")
            }

            override fun newBlock(height: Long) {
                Log.d(TAG, "newBlock height: $height")

                if (lastBlockTime < System.currentTimeMillis() - 2000) {
                    lastBlockTime = System.currentTimeMillis()
                    observer.onRefreshed(height)
                }
            }

            override fun updated() {
                Log.d(TAG, "updated")
            }

            override fun refreshed() {
                Log.d(TAG, "refreshed")

                val currentWallet = getWallet()
                if (currentWallet == null) {
                    observer.onWalletOpenFailed("wallet opened failed")
                    return
                }
                if (currentWallet.isSynchronized && !synced) {
                    val store = currentWallet.store()
                    if (store) {
                        synced = true
                    }
                }
                observer.onRefreshed(null)
            }
        })
        wallet.startRefresh()
        return false
    }

    fun refreshWallet() {
        getWallet()?.refresh()
    }

    fun stopRefresh() {
        getWallet()?.pauseRefresh()
        getWallet()?.setListener(null)
    }

    fun stopWallet() {
        getWallet()?.let {
            it.pauseRefresh()
            it.setListener(null)
            it.close()
        }
    }

    fun getWallet(): Wallet? = WalletManager.getInstance().wallet

    fun isRunning(): Boolean {
        val wallet = getWallet()
        return wallet != null && wallet.status == Wallet.Status.Status_Ok
    }

    fun isConnecting(): Boolean {
        val wallet = getWallet()
        return wallet != null && wallet.status == Wallet.Status.Status_Ok
                && wallet.connectionStatus == Wallet.ConnectionStatus.ConnectionStatus_Connected
    }

    fun getDaemonBlockChainHeight() = getWallet()?.daemonBlockChainHeight ?: 0

    fun getDaemonBlockChainTargetHeight() = getWallet()?.daemonBlockChainTargetHeight ?: 0

    fun getBlockChainHeight() = getWallet()?.blockChainHeight ?: 0

    fun getBalance() = getWallet()?.balance

    fun isSynchronized() = getWallet()?.isSynchronized ?: false

    fun getTransactionHistoryCount() = getWallet()?.history?.count ?: 0

    fun refreshTransactionHistory() {
        getWallet()?.history?.refresh()
    }

    fun getTransactionHistory(): List<io.wookey.wallet.data.entity.TransactionInfo> {
        val wallet = getWallet() ?: return emptyList()
        val all = wallet.history.all.sortedByDescending { it.timestamp }
        val allInfo = mutableListOf<io.wookey.wallet.data.entity.TransactionInfo>()
        all.forEach {
            allInfo.add(convert(it))
        }
        return allInfo
    }

    fun getDisplayAmount(amount: Long) = Wallet.getDisplayAmount(amount) ?: ""

    fun getAmountFromString(amount: String) = Wallet.getAmountFromString(amount)

    fun getIntegratedAddress(id: String) = getWallet()?.getIntegratedAddress(id) ?: ""

    fun generatePaymentId() = Wallet.generatePaymentId() ?: ""

    fun isAddressValid(address: String) = Wallet.isAddressValid(address)

    fun isPaymentIdValid(paymentId: String) = Wallet.isPaymentIdValid(paymentId)

    fun createTransaction(
            isAll: Boolean = false,
            dstAddress: String,
            paymentId: String?,
            amount: String, mixinCount: Int = 10,
            priority: PendingTransaction.Priority = PendingTransaction.Priority.Priority_Default
    ) {

        val wallet = getWallet() ?: throw IllegalStateException("wallet opened failed")

        val txData = TxData()
        txData.destinationAddress = dstAddress
        if (!paymentId.isNullOrBlank()) {
            txData.paymentId = paymentId
        } else {
            txData.paymentId = ""
        }
        txData.amount = if (isAll) Wallet.SWEEP_ALL else Wallet.getAmountFromString(amount)
        txData.mixin = mixinCount
        txData.priority = priority

        wallet.disposePendingTransaction()
        val pendingTransaction = wallet.createTransaction(txData)
        val status = pendingTransaction.status

        if (status != PendingTransaction.Status.Status_Ok) {
            val error = pendingTransaction.errorString
            wallet.disposePendingTransaction()
            throw IllegalStateException(error)
        }
    }

    fun sendTransaction(): String? {

        val wallet = getWallet() ?: throw IllegalStateException("wallet opened failed")

        val pendingTransaction = wallet.pendingTransaction
        val status = pendingTransaction.status

        if (status != PendingTransaction.Status.Status_Ok) {
            val error = pendingTransaction.errorString
            wallet.disposePendingTransaction()
            throw IllegalStateException(error)
        }

        val txId = pendingTransaction.firstTxId
        val success = pendingTransaction.commit("", true)
        if (success) {
            wallet.disposePendingTransaction()
            val store = wallet.store()
            if (!store) {
                Log.e(TAG, wallet.errorString)
            }
        } else {
            val error = pendingTransaction.errorString
            wallet.disposePendingTransaction()
            throw IllegalStateException(error)
        }
        return txId
    }

    fun getTxAmount(): String {
        val pendingTransaction = getWallet()?.pendingTransaction ?: return ""
        return getDisplayAmount(pendingTransaction.amount)
    }

    fun getTxFee(): String {
        val pendingTransaction = getWallet()?.pendingTransaction ?: return ""
        return getDisplayAmount(pendingTransaction.fee)
    }

    private fun convert(it: TransactionInfo): io.wookey.wallet.data.entity.TransactionInfo {
        return io.wookey.wallet.data.entity.TransactionInfo().apply {
            direction = it.direction.value
            isPending = it.isPending
            isFailed = it.isFailed
            amount = Wallet.getDisplayAmount(it.amount)
            fee = Wallet.getDisplayAmount(it.fee)
            blockHeight = it.blockheight
            confirmations = it.confirmations
            hash = it.hash
            timestamp = it.timestamp * 1000
            paymentId = it.paymentId
            txKey = it.txKey
            address = it.address
            subAddressLabel = it.subaddressLabel
        }
    }

    fun getBlockHeight(value: String) = RestoreHeight.getInstance().getHeight(value)

    fun testRpcService(url: String, timeout: Int = 3 * 1000): Long {
        var time = Long.MAX_VALUE
        val split = url.split(":")
        val connection = URL("http", split[0], split[1].toInt(), "json_rpc").openConnection() as? HttpURLConnection
            ?: throw IllegalArgumentException("url is invalid")
        connection.connectTimeout = timeout
        connection.readTimeout = timeout
        connection.doInput = true
        connection.doOutput = true
        connection.useCaches = false
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8")
        val current = System.currentTimeMillis()
        connection.connect()
        val body = "{\"jsonrpc\":\"2.0\",\"id\":\"0\",\"method\":\"getlastblockheader\"}"
        val writer = BufferedWriter(OutputStreamWriter(connection.outputStream, "UTF-8"))
        writer.write(body)
        writer.flush()
        writer.close()

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            time = System.currentTimeMillis() - current
            val buffer = StringBuffer()
            val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
            var line = ""
            while (reader.readLine()?.apply { line = this } != null) {
                buffer.append(line)
            }
            reader.close()
            val json = JSONObject(buffer.toString())
            val header = json.optJSONObject("result").optJSONObject("block_header")
            Log.e("wookey", "$url--->>> height: ${header.optLong("height")}")
            Log.e("wookey", "$url--->>> majorVersion: ${header.optLong("major_version")}")
        }
        connection.disconnect()
        return time
    }

    fun addSubAddress(label: String) {
        getWallet()?.addSubaddress(label)
        getWallet()?.store()
    }

    fun getSubAddresses(): List<SubAddress> {
        val list = getWallet()?.subaddresses ?: emptyList()
        val subAddress = mutableListOf<SubAddress>()
        list.forEach {
            subAddress.add(SubAddress(it.rowId, it.address, it.label))
        }
        return subAddress
    }

    fun getIndexByAddress(address: String): Int {
        val list = getWallet()?.subaddresses ?: emptyList()
        list.forEachIndexed { index, subaddressRow ->
            if (subaddressRow.address == address) {
                return index
            }
        }
        return -1
    }

    fun getLabelByAddress(address: String): String {
        val list = getWallet()?.subaddresses ?: emptyList()
        list.forEach { subaddressRow ->
            if (subaddressRow.address == address) {
                return subaddressRow.label
            }
        }
        return ""
    }

    fun setSubAddressLabel(label: String, index: Int) {
        getWallet()?.setSubaddressLabel(index, label)
        getWallet()?.store()
    }
}
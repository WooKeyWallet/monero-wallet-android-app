package io.wookey.wallet.core

import android.app.Application
import io.wookey.wallet.App
import io.wookey.wallet.data.AppDatabase
import io.wookey.wallet.data.entity.Asset
import io.wookey.wallet.data.entity.Wallet
import java.io.File

class XMRRepository(val context: Application = App.instance) {

    fun getKeysFilePath(name: String): String {
        return "${context.filesDir.absolutePath}${File.separator}wallet${File.separator}xmr${File.separator}$name${File.separator}$name.keys"
    }

    fun getWalletFilePath(name: String): String {
        return "${context.filesDir.absolutePath}${File.separator}wallet${File.separator}xmr${File.separator}$name${File.separator}$name"
    }

    private fun generateXMRFile(name: String): File {
        val dir = "${context.filesDir.absolutePath}${File.separator}wallet${File.separator}xmr${File.separator}$name"
        val walletFolder = File(dir)
        if (!walletFolder.exists()) {
            walletFolder.mkdirs()
        }
        val cacheFile = File(dir, name)
        val keysFile = File(walletFolder, "$name.keys")
        val addressFile = File(walletFolder, "$name..address.txt")
        if (cacheFile.exists() || keysFile.exists() || addressFile.exists()) {
            throw RuntimeException("Some wallet files already exist for $dir")
        }
        return cacheFile
    }

    fun createWallet(walletName: String, password: String): Wallet? {
        return XMRWalletController.createWallet(generateXMRFile(walletName), password)
    }

    fun recoveryWallet(walletName: String, password: String, mnemonic: String, restoreHeight: Long?): Wallet? {
        return XMRWalletController.recoveryWallet(generateXMRFile(walletName), password, mnemonic, restoreHeight
                ?: 0)
    }

    fun createWalletWithKeys(walletName: String, password: String, address: String, viewKey: String,
                             spendKey: String, restoreHeight: Long?): Wallet? {
        return XMRWalletController.createWalletWithKeys(generateXMRFile(walletName), password, restoreHeight
                ?: 0, address, viewKey, spendKey)
    }

    fun saveWallet(wallet: Wallet): Wallet? {
        val database = AppDatabase.getInstance()

        val count = database.walletDao().countWallets()
        wallet.isActive = count == 0
        database.walletDao().insertWallet(wallet)
        val insert = database.walletDao().getWalletsByName(wallet.symbol, wallet.name)
        if (insert != null) {
            database.assetDao().insertAsset(Asset(walletId = insert.id, token = insert.symbol))
        }
        return insert
    }

    fun cancelCreate(name: String) {
        val dir = "${context.filesDir.absolutePath}${File.separator}wallet${File.separator}xmr${File.separator}$name"
        val walletFolder = File(dir)
        if (walletFolder.exists() && walletFolder.isDirectory) {
            walletFolder.listFiles().forEach {
                if (it.isFile) {
                    it.delete()
                }
            }
        }
    }

}
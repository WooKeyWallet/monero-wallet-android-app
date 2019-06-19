package io.wookey.wallet.support

import io.wookey.wallet.App
import io.wookey.wallet.support.extensions.versionCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class VersionManager {

    private val uiScope = CoroutineScope(Dispatchers.Main)

    fun getLatestReleases() {
        uiScope.launch {
            val url = "https://api.github.com/repos/WooKeyWallet/wookey-wallet-android-app/releases/latest"
            try {
                withContext(Dispatchers.IO) {
                    getLatestReleases(URL(url))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getLatestReleases(url: URL) {
        try {
            val timeout: Int = 10 * 1000
            val connection = url.openConnection() as? HttpURLConnection
                ?: throw IllegalArgumentException("url is invalid")
            connection.connectTimeout = timeout
            connection.readTimeout = timeout
            connection.requestMethod = "GET"
            connection.doInput = true
            connection.useCaches = false
            connection.connect()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val buffer = StringBuffer()
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                var line = ""
                while (reader.readLine()?.apply { line = this } != null) {
                    buffer.append(line)
                }
                reader.close()
                val json = JSONObject(buffer.toString())
                var tagName = json.optString("tag_name")
                if (tagName.startsWith("v")) {
                    tagName = tagName.substring(1).trim()
                }
                // TODO: 2.10.3格式
                val tagNames = tagName.split(".")
                var tagCode = 0
                tagNames.forEachIndexed { index, s ->
                    tagCode += s.toInt().times(Math.pow(10.0, (tagNames.size - (index + 1)).toDouble())).toInt()
                }

                App.newVersion = tagCode > App.instance.versionCode()
            }
            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
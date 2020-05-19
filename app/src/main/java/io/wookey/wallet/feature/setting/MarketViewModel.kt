package io.wookey.wallet.feature.setting

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import io.wookey.wallet.base.BaseViewModel
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MarketViewModel : BaseViewModel() {

    val loading = MutableLiveData<Boolean>()

    val priceMap = MutableLiveData<Map<String, String>>()

    private var polling = true
    private lateinit var pollingJob: Job

    fun loadData(currency: String) {
        loading.value = true
        uiScope.launch {
            withContext(Dispatchers.IO) {
                request(currency)
            }
            loading.postValue(false)
            polling(currency)
        }
    }

    private fun polling(currency: String) {
        if (::pollingJob.isInitialized && !pollingJob.isCancelled) {
            pollingJob.cancel()
        }
        polling = true
        pollingJob = uiScope.launch {
            withContext(Dispatchers.IO) {
                while (polling) {
                    delay(1000 * 60)
                    request(currency)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (::pollingJob.isInitialized && !pollingJob.isCancelled) {
            pollingJob.cancel()
        }
    }

    private fun request(currency: String) {
        try {
            val url =
                "https://api.coingecko.com/api/v3/simple/price?ids=monero,bitcoin,litecoin,ethereum,eos&vs_currencies=${currency}"
            val timeout: Int = 10 * 1000
            val connection = URL(url).openConnection() as? HttpURLConnection
                ?: throw IllegalArgumentException("url is invalid")
            connection.connectTimeout = timeout
            connection.readTimeout = timeout
            connection.requestMethod = "GET"
            connection.doInput = true
            connection.useCaches = false
            connection.connect()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val buffer = StringBuffer()
                val reader =
                    BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                var line = ""
                while (reader.readLine()?.apply { line = this } != null) {
                    buffer.append(line)
                }
                reader.close()
                val s = buffer.toString()
                Log.e("json", s)
                val json = JSONObject(s)

                val monero = json.optJSONObject("monero")
                val moneroPrice = monero.optString(currency)

                val bitcoin = json.optJSONObject("bitcoin")
                val bitcoinPrice = bitcoin.optString(currency)

                val litecoin = json.optJSONObject("litecoin")
                val litecoinPrice = litecoin.optString(currency)

                val ethereum = json.optJSONObject("ethereum")
                val ethereumPrice = ethereum.optString(currency)

                val eos = json.optJSONObject("eos")
                val eosPrice = eos.optString(currency)

                val map = mutableMapOf<String, String>()
                map["XMR"] = moneroPrice
                map["BTC"] = bitcoinPrice
                map["LTC"] = litecoinPrice
                map["EOS"] = eosPrice
                map["ETH"] = ethereumPrice
                map["currency"] = currency

                priceMap.postValue(map)
            }
            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
            polling = false
        }
    }

}
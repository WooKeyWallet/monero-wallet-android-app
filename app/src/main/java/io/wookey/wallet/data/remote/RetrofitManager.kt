package io.wookey.wallet.data.remote

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import java.security.KeyStore
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSession

object RetrofitManager {
    const val CHANGELLY_API = "https://api.changelly.com/"
    const val API_KEY = "fbdbc96e3a9f42d99b6ef07b01f46c50"
    const val SECRET = "fd1f3f7abd5a942ae03c6b00b81cbb7e557638270f5070e58bcbfcb019dfd7c7"

    fun retrofit(okHttpClient: OkHttpClient = okHttpClient()): Retrofit =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(CHANGELLY_API)
            .addConverterFactory(
                Json(
                    JsonConfiguration(
                        ignoreUnknownKeys = true,
                        isLenient = true
                    )
                ).asConverterFactory(
                    MediaType.get("application/json; charset=UTF-8")
                )
            )
            .build()

    fun okHttpClient(): OkHttpClient {
        val clientBuilder = OkHttpClient().newBuilder()
            .addInterceptor {
                val request = it.request()
                val body = request.body()?.let { body -> requestBodyToString(body) }
                val builder = request.newBuilder()
                    .removeHeader("api-key")
                    .addHeader("api-key", API_KEY)
                    .removeHeader("sign")
                    .addHeader("sign", body?.hmacSHA512(SECRET) ?: "")
                it.proceed(builder.build())
            }
            .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })

//        try {
//            val ssl = SSLSocketFactoryImpl(KeyStore.getInstance(KeyStore.getDefaultType()))
//            clientBuilder.sslSocketFactory(ssl.sSlContext.socketFactory, ssl.trustManager)
//            clientBuilder.hostnameVerifier { hostname, session -> session.verify() }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
        return clientBuilder.build()
    }

    fun SSLSession.verify(): Boolean {
        try {
            listOf(
                "api.changelly.com"
            ).forEach {
                if (HttpsURLConnection.getDefaultHostnameVerifier().verify(it, this)) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun requestBodyToString(requestBody: RequestBody): String? {
        return try {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: Exception) {
            null
        }
    }

    val changellyService: ChangellyService by lazy { retrofit().create(ChangellyService::class.java) }
}
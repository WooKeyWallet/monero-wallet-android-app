package io.wookey.wallet.data.remote

import io.wookey.wallet.data.remote.entity.*
import retrofit2.http.Body
import retrofit2.http.POST

interface ChangellyService {

    @POST(".")
    suspend fun getCurrencies(@Body body: RPCRequest<Empty>): RPCResponse<List<String>>

    @POST(".")
    suspend fun getMinAmount(@Body body: RPCRequest<Map<String, String>>): RPCResponse<String>

    @POST(".")
    suspend fun getExchangeAmount(@Body body: RPCRequest<List<Map<String, String>>>): RPCResponse<List<ExchangeAmount>>

    @POST(".")
    suspend fun createTransaction(@Body body: RPCRequest<Map<String, String>>): RPCResponse<SwapCreateTransaction>

    @POST(".")
    suspend fun getTransactionByID(@Body body: RPCRequest<Map<String, String>>): RPCResponse<List<SwapTransaction>>
}
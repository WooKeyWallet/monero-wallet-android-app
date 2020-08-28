package io.wookey.wallet.data.remote.entity

import kotlinx.serialization.Serializable

@Serializable
data class RPCResponse<T>(
    val jsonrpc: String,
    val id: String,
    val result: T? = null,
    val error: RPCError? = null
)
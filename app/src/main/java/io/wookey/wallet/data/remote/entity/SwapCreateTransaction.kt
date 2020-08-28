package io.wookey.wallet.data.remote.entity

import kotlinx.serialization.Serializable

@Serializable
data class SwapCreateTransaction(
    val id: String,
    val createdAt: String,
    val currencyFrom: String,
    val currencyTo: String,
    val amountExpectedFrom: String,
    val payinAddress: String,
    val payoutAddress: String?,
    val payoutExtraId: String? = null,
    val amountExpectedTo: String?
)

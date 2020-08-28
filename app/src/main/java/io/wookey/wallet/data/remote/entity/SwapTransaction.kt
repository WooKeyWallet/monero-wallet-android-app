package io.wookey.wallet.data.remote.entity

import kotlinx.serialization.Serializable

@Serializable
data class SwapTransaction(
    val id: String,
    val createdAt: Long,
    val moneyReceived: String?,
    val moneySent: String?,
    val status: String,
    val currencyFrom: String,
    val currencyTo: String,
    val amountExpectedFrom: String?,
    val payoutAddress: String?,
    val payoutExtraId: String?,
    val amountFrom: String?,
    val amountTo: String?,
    val amountExpectedTo: String?,
    val networkFee: String?,
    val changellyFee: String?,
    val apiExtraFee: String?,
    val totalFee: String?,
    @Transient
    var addressTag: String? = null
)

package io.wookey.wallet.data.remote.entity

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeAmount(
    val from: String,
    val to: String,
    val networkFee: String? = null,
    val amount: String? = null,
    val result: String? = null,
    val visibleAmount: String? = null,
    val rate: String? = null,
    val fee: String? = null
)

//  "from": "xmr",
//  "to": "btc",
//  "networkFee": "0.0002500000000000000000",
//  "amount": "1",
//  "result": "0.00768718",
//  "visibleAmount": "0.00771805715863453815",
//  "rate": "0.00771805715863453815",
//  "fee": "0.0000308722286345381526"
package io.wookey.wallet.data.remote

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun String.md5(): String {
    return hashString(this, "MD5")
}

fun String.sha256(): String {
    return hashString(this, "SHA-256")
}

fun String.sha512(): String {
    return hashString(this, "SHA-512")
}

private fun hashString(message: String, algorithm: String): String {
    return MessageDigest
        .getInstance(algorithm)
        .digest(message.toByteArray())
        .hexDigest()
}

fun String.hmacMD5(key: String): String {
    return hmacString(this, key, "HmacMD5")
}

fun String.hmacSHA256(key: String): String {
    return hmacString(this, key, "HmacSHA256")
}

fun String.hmacSHA512(key: String): String {
    return hmacString(this, key, "HmacSHA512")
}

private fun hmacString(message: String, key: String, algorithm: String): String {
    return Mac.getInstance(algorithm)
        .apply { init(SecretKeySpec(key.toByteArray(), algorithm)) }
        .doFinal(message.toByteArray())
        .hexDigest()
}

private fun ByteArray.hexDigest(): String {
    return fold("", { str, it -> str + "%02x".format(it) })
}
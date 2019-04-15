package io.wookey.wallet.support.extensions

import android.content.Context
import android.content.SharedPreferences
import io.wookey.wallet.App

fun sharedPreferences(name: String = "default") = App.instance.getSharedPreferences(name, Context.MODE_PRIVATE)
        ?: throw IllegalStateException("SharedPreferences initialized failed")

fun SharedPreferences.edit(action: SharedPreferences.Editor.() -> Unit) {
    edit().apply {
        action()
    }.apply()
}

fun SharedPreferences.putString(key: String, value: String) {
    edit { putString(key, value) }
}

fun SharedPreferences.putInt(key: String, value: Int) {
    edit { putInt(key, value) }
}

fun SharedPreferences.putBoolean(key: String, value: Boolean) {
    edit { putBoolean(key, value) }
}

fun SharedPreferences.putFloat(key: String, value: Float) {
    edit { putFloat(key, value) }
}

fun SharedPreferences.putLong(key: String, value: Long) {
    edit { putLong(key, value) }
}

fun SharedPreferences.putStringSet(key: String, value: Set<String>) {
    edit { putStringSet(key, value) }
}

fun SharedPreferences.remove(key: String) {
    edit { remove(key) }
}

fun SharedPreferences.clear() {
    edit { clear() }
}
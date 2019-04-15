package io.wookey.wallet

import android.app.Activity

class ActivityStackManager private constructor() {

    private var activityStack = mutableListOf<Activity>()

    companion object {

        @Volatile
        private var INSTANCE: ActivityStackManager? = null

        fun getInstance(): ActivityStackManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ActivityStackManager().also { INSTANCE = it }
            }

    }

    fun add(activity: Activity?) {
        synchronized(activityStack) {
            if (activity == null) {
                return
            }
            activityStack.add(activity)
        }
    }

    fun remove(activity: Activity?) {
        synchronized(activityStack) {
            if (activity == null) {
                return
            }
            activityStack.remove(activity)
        }
    }

    @Synchronized
    fun all(): List<Activity> {
        return activityStack
    }

    @Synchronized
    fun contain(cls: Class<out Activity>): Boolean {
        var contain = false
        val iterator = activityStack.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.javaClass == cls) {
                contain = true
                break
            }
        }
        return contain
    }

    @Synchronized
    fun get(cls: Class<out Activity>): Activity? {
        val iterator = activityStack.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.javaClass == cls) {
                return next
            }
        }
        return null
    }

    fun finishActivity(cls: Class<out Activity>) {
        synchronized(activityStack) {
            val iterator = activityStack.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next.javaClass == cls) {
                    next.finish()
                    break
                }
            }
        }
    }

    fun finishToActivity(cls: Class<out Activity>, selfFinished: Boolean = true) {
        synchronized(activityStack) {
            var removeStart = activityStack.size
            val iterator = activityStack.iterator()
            var index = 0
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (removeStart < index) {
                    if (index < activityStack.size - 1) {
                        next.finish()
                    } else {
                        if (selfFinished) {
                            next.finish()
                        }
                    }
                }
                if (next.javaClass == cls) {
                    removeStart = index
                }
                index++
            }
        }
    }

}
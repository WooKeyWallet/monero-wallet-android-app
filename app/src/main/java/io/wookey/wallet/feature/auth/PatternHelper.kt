package io.wookey.wallet.feature.auth

import android.text.TextUtils

class PatternHelper() {

    var message: Int = 0
        private set
    var tmpPwd: String? = null
    private var times = 0
    var isFinish = false
        private set
    var isOk = false
        private set

    val remainTimes: Int
        get() = if (times < 5) MAX_TIMES - times else 0

    fun clearState() {
        isFinish = false
        isOk = false
        tmpPwd = null
    }

    fun validateForSetting(hitIndexList: List<Int>?, block: (String?) -> Unit) {
        isFinish = false
        isOk = false
        if (hitIndexList == null || hitIndexList.size < MAX_SIZE) {
            message = CODE_SIZE_ERROR
            return
        }
        //1. draw first time
        if (TextUtils.isEmpty(tmpPwd)) {
            tmpPwd = convert2String(hitIndexList)
            message = CODE_REDRAW
            isOk = true
            return
        }
        //2. draw second times
        if (tmpPwd == convert2String(hitIndexList)) {
            message = CODE_SETTING_SUCCESS
            isOk = true
            isFinish = true
            block(tmpPwd)
        } else {
            message = CODE_DIFF_PRE_ERROR
        }
    }

    fun validateForChecking(hitIndexList: List<Int>?, storagePwd: String?) {
        isOk = false
        if (hitIndexList == null || hitIndexList.size < MAX_SIZE) {
            message = CODE_SIZE_ERROR
            return
        }
        if (!TextUtils.isEmpty(storagePwd) && storagePwd == convert2String(hitIndexList)) {
            message = CODE_CHECKING_SUCCESS
            isOk = true
            isFinish = true
        } else {
            times++
            isFinish = times > MAX_SIZE
            message = CODE_PWD_ERROR
        }
    }

    private fun convert2String(hitIndexList: List<Int>): String {
        return hitIndexList.toString()
    }

    companion object {
        const val MAX_SIZE = 4
        const val MAX_TIMES = 5

        const val CODE_REDRAW = 0
        const val CODE_SETTING_SUCCESS = 1
        const val CODE_SIZE_ERROR = 2
        const val CODE_DIFF_PRE_ERROR = 3
        const val CODE_CHECKING_SUCCESS = 4
        const val CODE_PWD_ERROR = 5
    }
}
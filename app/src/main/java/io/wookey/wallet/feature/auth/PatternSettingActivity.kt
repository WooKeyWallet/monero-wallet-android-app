package io.wookey.wallet.feature.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.github.ihsg.patternlocker.OnPatternChangeListener
import com.github.ihsg.patternlocker.PatternLockerView
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.utils.StatusBarHelper
import kotlinx.android.synthetic.main.activity_pattern_setting.*

class PatternSettingActivity : BaseTitleSecondActivity() {

    private val patternHelper = PatternHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pattern_setting)

        setCenterTitle(R.string.pattern_setting_title)

        setRightTitle(R.string.pattern_reset)

        setRightTitleClick(View.OnClickListener {
            patternIndicatorView.updateState(null, false)
            patternLockerView.clearHitState()
            patternHelper.clearState()
            msg.setText(R.string.pattern_canvas_first)
            msg.setTextColor(ContextCompat.getColor(this, R.color.color_333333))
        })

        patternIndicatorView.normalCellView = NormalCellView().setRadius(dp2px(4f))
        patternIndicatorView.hitCellView = IndicatorHitCellView().setRadius(dp2px(4f))
        patternIndicatorView.linkedLineView = IndicatorLinkedLineView()

        patternLockerView.normalCellView = NormalCellView().setRadius(dp2px(8f))
        patternLockerView.hitCellView = RippleLockerHitCellView()

        patternLockerView.setOnPatternChangedListener(object : OnPatternChangeListener {
            override fun onStart(view: PatternLockerView) {}

            override fun onChange(view: PatternLockerView, hitIndexList: List<Int>) {
                if (patternHelper.tmpPwd.isNullOrBlank()) {
                    patternIndicatorView.updateState(hitIndexList, false)
                }
            }

            override fun onComplete(view: PatternLockerView, hitIndexList: List<Int>) {
                val isError = !isPatternOk(hitIndexList)
                view.updateStatus(isError)
                updateMsg()
            }

            override fun onClear(view: PatternLockerView) {
            }
        })

    }

    override fun onNavigationClick(v: View?) {
        setPatternResult()
        super.onNavigationClick(v)
    }

    override fun onBackPressed() {
        setPatternResult()
        super.onBackPressed()
    }

    private fun setPatternResult(pattern: String = "") {
        val intent = Intent()
        intent.putExtra("pattern", pattern)
        setResult(Activity.RESULT_OK, intent)
    }

    private fun isPatternOk(hitIndexList: List<Int>): Boolean {
        patternHelper.validateForSetting(hitIndexList) {
            if (patternHelper.isFinish && !it.isNullOrBlank()) {
                setPatternResult(it)
                finish()
            }
        }
        return patternHelper.isOk
    }

    private fun updateMsg() {
        when (patternHelper.message) {
            PatternHelper.CODE_REDRAW -> {
                msg.text = getString(R.string.pattern_canvas_second)
            }
            PatternHelper.CODE_SETTING_SUCCESS -> {
                msg.text = getString(R.string.pattern_canvas_success)
            }
            PatternHelper.CODE_SIZE_ERROR -> {
                msg.text =
                    getString(R.string.pattern_canvas_failed, PatternHelper.MAX_SIZE.toString())
            }
            PatternHelper.CODE_DIFF_PRE_ERROR -> {
                msg.text = getString(R.string.pattern_canvas_not_match)
            }
        }
        msg.setTextColor(
            if (patternHelper.isOk)
                ContextCompat.getColor(this, R.color.color_333333)
            else
                ContextCompat.getColor(this, R.color.color_FF3A5C)
        )
    }
}
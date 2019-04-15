package io.wookey.wallet.feature.address

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import cn.bingoogolapple.qrcode.core.QRCodeView
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseActivity
import io.wookey.wallet.widget.IOSDialog
import io.wookey.wallet.support.REQUEST_CODE_PERMISSION_CAMERA
import io.wookey.wallet.support.extensions.dp2px
import io.wookey.wallet.support.utils.StatusBarHelper
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.android.synthetic.main.base_title_second.*

class ScanActivity : BaseActivity(), QRCodeView.Delegate {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        StatusBarHelper.setStatusBarLightMode(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }
        centerTitle.text = getString(R.string.scan)

        zxingView.setDelegate(this)

        if (hasPermissions(this, Manifest.permission.CAMERA)) {
            showScanView()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSION_CAMERA)
        }
    }

    override fun onStop() {
        zxingView.stopCamera()
        super.onStop()
    }

    override fun onDestroy() {
        zxingView.onDestroy()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showScanView()
            } else {
                showDialog()
            }
        }
    }

    override fun onScanQRCodeSuccess(result: String?) {
        zxingView.postDelayed({ setSelectResult(result) }, 100)
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {

    }

    override fun onScanQRCodeOpenCameraError() {
        showDialog()
    }

    private fun showScanView() {
        zxingView.startCamera()
        zxingView.startSpotAndShowRect()
    }

    private fun showDialog() {
        IOSDialog(this)
                .radius(dp2px(5))
                .titleText(getString(R.string.dialog_permissions_camera))
                .hasContentView(false)
                .rightText(getString(R.string.confirm))
                .setIOSDialogRightListener { finish() }
                .isOnlyRight(true)
                .cancelAble(false)
                .layout()
                .show()
    }

    private fun setSelectResult(result: String?) {
        val intent = Intent()
        intent.putExtra("result", result)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun hasPermissions(context: Context?, vararg perms: String): Boolean {
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // DANGER ZONE!!! Changing this will break the library.
            return true
        }

        // Null context may be passed if we have detected Low API (less than M) so getting
        // to this point with a null context should not be possible.
        if (context == null) {
            throw IllegalArgumentException("Can't check permissions for null context")
        }

        for (perm in perms) {
            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }
}

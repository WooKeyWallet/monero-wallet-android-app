package io.wookey.wallet.feature.setting

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import io.wookey.wallet.R
import io.wookey.wallet.base.BaseTitleSecondActivity
import io.wookey.wallet.support.extensions.getCurrentLocale
import io.wookey.wallet.widget.SlowlyProgressBar
import kotlinx.android.synthetic.main.activity_webview.*


class WebViewActivity : BaseTitleSecondActivity() {

    private lateinit var mWebView: WebView
    private var mTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val slowlyProgressBar = SlowlyProgressBar(progressBar)

        mWebView = WebView(this)
        container.addView(mWebView)
        val settings = mWebView.getSettings()
        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true

        // 自适应
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        // 禁用缩放
        settings.displayZoomControls = false
        settings.builtInZoomControls = false

        // 禁用文字缩放
        settings.textZoom = 100

        // 缓存
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.domStorageEnabled = true
        settings.setAppCachePath(cacheDir.absolutePath)
        settings.setAppCacheEnabled(true)

        // 允许加载本地html文件
        settings.allowFileAccess = true
        // 禁止js访问本地文件
        settings.allowFileAccessFromFileURLs = false
        settings.allowUniversalAccessFromFileURLs = false

        settings.defaultTextEncodingName = "utf-8"
        settings.loadsImagesAutomatically = true

        // 自动开启声音
        settings.mediaPlaybackRequiresUserGesture = false

        // 5.0以上开启混合模式加载
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
        }

        mWebView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                try {
                    val uri = Uri.parse(url)
                    if ("mailto".equals(uri.scheme, ignoreCase = true)
                            || "tel".equals(uri.scheme, ignoreCase = true)
                            || "sms".equals(uri.scheme, ignoreCase = true)) {
                        //注意这里Intent Action的写法。
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(intent)
                        return true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (!TextUtils.equals(mTitle, url)) {
                    setCenterTitle(mTitle)
                }
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                val builder = AlertDialog.Builder(view.context)
                builder.setMessage(R.string.dialog_title_ssl_error)
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        handler.proceed()
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        handler.cancel()
                    }
                val dialog = builder.create()
                dialog.show()
            }
        }

        mWebView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                slowlyProgressBar.onProgressChange(newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                mTitle = title ?: ""
                writeData(getCurrentLocale())
            }
        }
        mWebView.loadUrl("https://wallet.wookey.io/service-docs/app.html")

        setRightIcon(R.drawable.icon_refresh)
        setRightIconClick(View.OnClickListener { mWebView.reload() })
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mWebView?.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            mWebView?.clearHistory()
            mWebView?.removeAllViews()
            val parent = mWebView.parent as? ViewGroup
            parent?.removeView(mWebView)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mWebView?.destroy()
        }
    }

    fun writeData(lang: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript("window.localStorage.setItem('language','$lang');") { value -> Log.e("setItem: %s", value) }
        } else {
            mWebView.loadUrl("javascript:localStorage.setItem('language','$lang');")
        }
    }
}
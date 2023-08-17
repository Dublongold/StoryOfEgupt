package com.example.storyofegupt.webScreen

import android.Manifest;
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.storyofegupt.R
import com.example.storyofegupt.network.ReceiverObject
import org.koin.android.ext.android.inject
import java.io.File
import java.io.IOException

class WebActivity: AppCompatActivity() {
    private val receiverObject: ReceiverObject by inject()
    var mainWebView: WebView? = null
    var mainFilePathCallback: ValueCallback<Array<Uri>>? = null
    var calback: Uri? = null

    private var someUrl: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_web)

        mainWebView = findViewById(R.id.singleWebView)
        someUrl = receiverObject.url
        configureWebView()
        onBackPressedDispatcher.addCallback {
            if(mainWebView!!.canGoBack()) {
                mainWebView!!.goBack()
            }
        }
    }

    private fun configureWebView() {
        configureSetting()
        CookieManager.getInstance().run {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(mainWebView, true)
        }

        mainWebView!!.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                mainFilePathCallback = filePathCallback
                return true
            }
        }
        mainWebView!!.webViewClient = MainWebViewClient()
        mainWebView!!.loadUrl(someUrl!!)
    }

    private fun configureSetting() {
        mainWebView?.run {
            settings.run {
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                allowFileAccess = true
                mixedContentMode = 0
                cacheMode = WebSettings.LOAD_DEFAULT
                userAgentString = mainWebView!!.settings.userAgentString.replace("; wv", "")
            }
            settings.run {
                javaScriptCanOpenWindowsAutomatically = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                javaScriptEnabled = true
                databaseEnabled = true
                allowUniversalAccessFromFileURLs = true
            }
        }
    }

    inner class MainWebViewClient : WebViewClient() {
        private var content: Boolean? = null
        private var method: String? = null
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val tempUri = request.url.toString()
            return if (tempUri.indexOf("/") != -1) {
                if (tempUri.indexOf("intent://ti/p/") != -1 && tempUri.indexOf("#") != -1) {
                    var someNewUrl = "line://ti/p/@"
                    someNewUrl += tempUri.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[1]
                    someNewUrl = someNewUrl.split("#Inten".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0]
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(someNewUrl)))
                    true
                } else {
                    content = !tempUri.contains("http")
                    if (!content!!) content!!
                    else {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(tempUri)))
                        true
                    }
                }
            } else true
        }

        override fun onReceivedLoginRequest(
            view: WebView,
            realm: String,
            account: String?,
            args: String
        ) {
            method = "OnReceivedLoginReq"
            super.onReceivedLoginRequest(view, realm, account, args)
        }
    }

    val requestPermissionLauncher = registerForActivityResult<String, Boolean>(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean? ->
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        var photoFile: File? = null
        try {
            photoFile = File.createTempFile("file", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        } catch (e: IOException) {
            Log.i("PhotoFile", "Unable to read file.")
        }
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
        calback = Uri.fromFile(photoFile)
        val old = Intent(Intent.ACTION_GET_CONTENT).also {
            it.addCategory(Intent.CATEGORY_OPENABLE)
            it.type = "*/*"
        }
        val intentArray = arrayOf(takePictureIntent)
        val chooserIntent = Intent(Intent.ACTION_CHOOSER).also {
            it.putExtra(Intent.EXTRA_INTENT, old)
            it.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
        }
        startActivityForResult(chooserIntent, 1)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        fun someCheck() = if (calback != null) arrayOf(calback!!)  else null

        mainFilePathCallback?.run {
            onReceiveValue(if (resultCode == -1) {
                if (data != null) {
                    val d = data.dataString
                    if (d != null) {
                        val u = Uri.parse(d)
                        arrayOf(u)
                    } else someCheck()
                } else someCheck()
            } else null)
        }
        mainFilePathCallback = null
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mainWebView!!.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mainWebView!!.restoreState(savedInstanceState)
    }
}
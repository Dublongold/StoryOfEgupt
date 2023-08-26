package com.stor.storyofegupt.mainActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse
import com.android.installreferrer.api.InstallReferrerStateListener
import com.facebook.applinks.AppLinkData
import com.stor.storyofegupt.R
import com.stor.storyofegupt.gameScreen.GameFragment
import com.stor.storyofegupt.gameScreen.GameViewModel
import com.stor.storyofegupt.helpScreen.HelpFragment
import com.stor.storyofegupt.loadingScreen.LoadingFragment
import com.stor.storyofegupt.menuScreen.MenuFragment
import com.stor.storyofegupt.network.NetworkRequest
import com.stor.storyofegupt.network.ReceiverObject
import com.stor.storyofegupt.webScreen.WebActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainActivity : AppCompatActivity() {
    private val vm: MainViewModel by viewModels()
    private val onNullCallback = OnNullCallback()
    private lateinit var receiverObject: ReceiverObject

    private val actionForPlay = {
        vm.changeDestination(PLAY_DESTINATION)
        true
    }
    private val actionForHelp = {
        vm.changeDestination(HELP_DESTINATION)
        true
    }
    private val goBackAction = {
        vm.changeDestination(MENU_DESTINATION)
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val flag = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        window.setFlags(flag, flag)

        if (GlobalContext.getOrNull() == null) {
            startKoin {
                modules(
                    module {
                        single { onNullCallback }
                        viewModelOf(::GameViewModel)
                        single { receiverObject }
                    }
                )
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val tUrl = getUrl()
                if(tUrl.isNullOrEmpty() || tUrl == "null" || !tUrl.contains("http")) {
                    // Ссылки в sharedPreferences нету.
                    val referrerClient = InstallReferrerClient.newBuilder(this@MainActivity).build()
                    // Делает реф запрос.
                    referrerClient.startConnection(
                         object: InstallReferrerStateListener {
                            private val TAG = "Install referrer state listener"
                            override fun onInstallReferrerSetupFinished(p0: Int) {
                                when(p0) {
                                    InstallReferrerResponse.OK -> {
                                        val r  = referrerClient.installReferrer.installReferrer
                                        Log.i(TAG, "Connection is OK. $r")
                                        referrerClient.endConnection()
                                        // Делаем диплинк запрос
                                        AppLinkData.fetchDeferredAppLinkData(this@MainActivity
                                        ) { appLinkData: AppLinkData? ->
                                            Log.e("DEEP", "SRABOTAL")
                                            val tree = appLinkData?.argumentBundle?.getString("target_url")
                                            val uri = tree?.split("$")
                                            var url: String? = null
                                            uri?.let { u ->
                                                url = "https://$u"
                                            }
                                            // Делаем запрос на сервер.
                                            makeSecondRequest(url, r)
                                        }
                                    }
                                    else -> {
                                        referrerClient.endConnection()
                                        vm.changeDestination(MENU_DESTINATION)
                                    }
                                }
                            }
                            override fun onInstallReferrerServiceDisconnected() {
                                Log.i(TAG, "Disconnected.")
                            }
                        }
                    )
                }
                else {
                    receiverObject = ReceiverObject(tUrl)
                    startActivity(Intent(this@MainActivity, WebActivity::class.java))
                }
            }
            catch(_: Exception) {}
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.destination.collect {
                    val frag: Fragment? = when(it) {
                        LOADING_DESTINATION -> LoadingFragment()
                        MENU_DESTINATION -> MenuFragment(actionForPlay, actionForHelp)
                        HELP_DESTINATION -> HelpFragment(goBackAction)
                        PLAY_DESTINATION -> GameFragment(goBackAction)
                        else -> null
                    }
                    if(frag != null) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, frag)
                            .commit()
                    }
                }
            }
        }
        supportFragmentManager.beginTransaction()
            .add(R.id.container, LoadingFragment())
            .commit()
    }

    private fun getUrl() = getSharedPreferences("url", MODE_PRIVATE).getString("url", null)
    private fun saveUrl(value: String) = getSharedPreferences("url", MODE_PRIVATE).edit().putString("url", value).apply()

    private fun makeSecondRequest(deep: String?, referrer: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val response = NetworkRequest().getContent(NetworkRequest.URL_FOR_TEST, deep, referrer)
            if (!response.isNullOrEmpty() && response != "null" && response.contains("http")) {
                saveUrl(response)
                receiverObject = ReceiverObject(response)
                startActivity(Intent(this@MainActivity, WebActivity::class.java))
            }
            else {
                vm.changeDestination(MENU_DESTINATION)
            }
        }
    }

    inner class OnNullCallback {
        lateinit var callback: () -> Boolean
    }
    companion object {
        const val LOADING_DESTINATION = "loading"
        const val MENU_DESTINATION = "menu"
        const val PLAY_DESTINATION = "play"
        const val HELP_DESTINATION = "help"
    }
}
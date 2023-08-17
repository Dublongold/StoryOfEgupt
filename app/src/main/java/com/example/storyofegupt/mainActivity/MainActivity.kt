package com.example.storyofegupt.mainActivity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.storyofegupt.R
import com.example.storyofegupt.gameScreen.GameFragment
import com.example.storyofegupt.gameScreen.GameViewModel
import com.example.storyofegupt.helpScreen.HelpFragment
import com.example.storyofegupt.loadingScreen.LoadingFragment
import com.example.storyofegupt.menuScreen.MenuFragment
import com.example.storyofegupt.network.NetworkRequest
import com.example.storyofegupt.network.ReceiverObject
import com.example.storyofegupt.webScreen.WebActivity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainActivity : AppCompatActivity() {
    private val vm: MainViewModel by viewModels()
    private val onNullCallback = OnNullCallback()

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

        startKoin {
            modules(
                module {
                    single { onNullCallback }
                    viewModelOf(::GameViewModel)
                }
            )
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val response = NetworkRequest(this@MainActivity).getContent(NetworkRequest.URL_FOR_TEST)
            if(!response.isNullOrEmpty() && response.contains("link")) {
                val fromJsonString = if(response[response.indexOf("link") - 1] != '"') {
                    response.replace("pusk", "\"pusk\"")
                        .replace("link", "\"link\"")
                }
                else {
                    response
                }.replace("pusk", "allow").replace("link", "url")
                val obj = Gson().fromJson(fromJsonString, ReceiverObject::class.java)
                delay(2000) // For imitation a long loading.
                if(obj.url != null) {
                    startActivity(Intent(this@MainActivity, WebActivity::class.java))
                    return@launch
                }
            }
            vm.changeDestination(MENU_DESTINATION)
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
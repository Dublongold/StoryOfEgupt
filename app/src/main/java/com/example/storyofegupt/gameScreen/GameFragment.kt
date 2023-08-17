package com.example.storyofegupt.gameScreen

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.storyofegupt.R
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class GameFragment(private val goBackAction: () -> Boolean): Fragment() {
    private val vm: GameViewModel by inject()
    private lateinit var btv: TextView
    private lateinit var rctv: TextView
    private lateinit var items: List<ImageView>

    val balance
        get() = vm.balance.value

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.setStartBalance(activity?.getSharedPreferences("balance", Activity.MODE_PRIVATE))
        btv = view.findViewById(R.id.balanceInfo)
        rctv = view.findViewById(R.id.retryCost)
        items = listOf(
            view.findViewById(R.id.item1),
            view.findViewById(R.id.item2),
            view.findViewById(R.id.item3)
        )
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.balance.collect {
                    btv.text = getString(R.string.balance_text, it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.retryCost.collect {
                    rctv.text = getString(R.string.retry_cost, it)
                }
            }
        }

        view.findViewById<TextView>(R.id.plusButton).setOnClickListener {
            vm.increaseRetryCost()
        }

        view.findViewById<TextView>(R.id.minusButton).setOnClickListener {
            vm.decreaseRetryCost()
        }

        view.findViewById<AppCompatButton>(R.id.goBackButton).setOnClickListener {
            goBackAction()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.itemsVisibility.collect {
                    for((ind, elem) in it.withIndex()) {
                        if(!elem) {
                            items[ind].setImageResource(R.drawable.empty_item)
                        }
                        else {
                            items[ind].setImageResource(when(vm.items.value[ind]) {
                                0 -> R.drawable.item_10
                                1 -> R.drawable.item_j
                                2 -> R.drawable.item_q
                                3 -> R.drawable.item_k
                                else -> throw IndexOutOfBoundsException()
                            })
                        }
                    }
                }
            }
        }
        for((ind, item) in items.withIndex()) {
            item.setOnClickListener {
                vm.checkItem(ind)
            }
        }

        view.findViewById<AppCompatButton>(R.id.retryButton).setOnClickListener {
            vm.retry()
            activity?.getSharedPreferences("balance", Activity.MODE_PRIVATE)?.edit()?.putInt("balance", balance)?.apply()
        }
    }
}
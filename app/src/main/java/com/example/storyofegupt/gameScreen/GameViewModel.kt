package com.example.storyofegupt.gameScreen

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class GameViewModel: ViewModel() {
    private val _balance = MutableStateFlow(10000)
    val items: MutableStateFlow<List<Int>> = MutableStateFlow(List(3) { Random.nextInt(0, 4) })
    val itemsVisibility: MutableStateFlow<List<Boolean>> = MutableStateFlow(List(3) {false})
    val balance
        get() = _balance.asStateFlow()

    private val _retryCost = MutableStateFlow(10)
    val retryCost
        get() = _retryCost.asStateFlow()

    fun setStartBalance(share: SharedPreferences?) {
        share?.run {
            val sb = getInt("balance", -1)
            if(sb != -1) {
                _balance.value = sb
            }
        }
    }

    fun increaseRetryCost() {
        if(_retryCost.value + 10 <= _balance.value) {
            _retryCost.update { it + 10 }
        }
    }
    fun decreaseRetryCost() {
        if(_retryCost.value >= 10) {
            _retryCost.update { it - 10 }
        }
    }

    fun retry() {
        if(_balance.value - _retryCost.value > 0) {
            _balance.value -= _retryCost.value
            items.value = List(3) { Random.nextInt(0, 4) }
            itemsVisibility.value = List(3) {false}

        }
    }

    fun checkItem(id: Int) {
        if(!itemsVisibility.value[id]) {
            itemsVisibility.update {
                it.mapIndexed { ind, elem ->
                    if(ind == id && !elem) true else elem
                }
            }
            if(itemsVisibility.value.all { it }) {
                if(items.value.all { it == items.value.first()}) {
                    _balance.value += (items.value.first() + 1) * (_retryCost.value + 10)
                }
            }
        }
    }
}
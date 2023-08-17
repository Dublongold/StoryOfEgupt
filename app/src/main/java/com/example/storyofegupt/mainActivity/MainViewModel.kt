package com.example.storyofegupt.mainActivity

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel: ViewModel() {
    private val _destination: MutableStateFlow<String?> = MutableStateFlow(null)
    val destination
        get() = _destination.asStateFlow()
    fun changeDestination(newDestination: String) {
        _destination.value = newDestination
    }
}
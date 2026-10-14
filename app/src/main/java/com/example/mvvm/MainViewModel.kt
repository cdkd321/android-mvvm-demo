package com.example.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private var count = 0

    private val _displayText = MutableLiveData("Hello MVVM")
    val displayText: LiveData<String> = _displayText

    fun increment() {
        count++
        _displayText.value = "Count: $count"
    }
}

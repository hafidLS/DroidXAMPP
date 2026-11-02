package com.droidxampp.termux

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object TermuxResultStore {
    private val _latest = MutableStateFlow("Belum ada perintah dijalankan.")
    val latest = _latest.asStateFlow()

    private val _monitor = MutableStateFlow("")
    val monitor = _monitor.asStateFlow()

    fun add(value: String) {
        _latest.value = value.takeLast(5000)
    }

    fun addMonitor(value: String) {
        _monitor.value = value.takeLast(12000)
    }
}

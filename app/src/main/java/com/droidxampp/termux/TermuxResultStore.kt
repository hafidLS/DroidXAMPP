package com.droidxampp.termux

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object TermuxResultStore {
    private val _latest = MutableStateFlow("Belum ada perintah dijalankan.")
    val latest = _latest.asStateFlow()
    fun add(value: String) { _latest.value = value.takeLast(5000) }
}

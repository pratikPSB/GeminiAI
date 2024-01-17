package com.psb.geminiai.ui.base

import androidx.lifecycle.ViewModel
import com.psb.geminiai.service.AppDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class BaseViewModel : ViewModel() {
    lateinit var appDelegate: AppDelegate
    private val job by lazy { SupervisorJob() }
    val uiScope by lazy { CoroutineScope(Dispatchers.Main + job) }
    val ioScope by lazy { CoroutineScope(Dispatchers.IO + job) }

    override fun onCleared() {
        uiScope.cancel()
        ioScope.cancel()
        super.onCleared()
    }
}
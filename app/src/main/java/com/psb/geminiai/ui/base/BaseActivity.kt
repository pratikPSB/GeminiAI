package com.psb.geminiai.ui.base

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.psb.geminiai.service.AppDelegate
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<VM : ViewModel, B : ViewBinding> : AppCompatActivity() {
    val activity by lazy { this }
    val appDelegate by lazy { application as AppDelegate }
    protected lateinit var viewModel: VM
    protected lateinit var binding: B

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[getViewModelClass()]
        (viewModel as BaseViewModel).appDelegate = appDelegate
        binding = getViewBinding()
        setContentView(binding.root)
    }

    private fun getViewModelClass(): Class<VM> {
        val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
        return type as Class<VM>
    }

    abstract fun getViewBinding(): B
}
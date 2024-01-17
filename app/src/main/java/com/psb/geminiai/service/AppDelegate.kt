package com.psb.geminiai.service

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.material.color.DynamicColors
import com.psb.geminiai.R
import com.yydcdut.markdown.MarkdownProcessor
import com.yydcdut.markdown.syntax.text.TextFactory

class AppDelegate : Application() {
    private var isAppInForeGround: Boolean = false
    val generativeProModel by lazy {
        GenerativeModel(
            // For text-only input, use the gemini-pro model
            modelName = "gemini-pro",
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = getString(R.string.apiKey)
        )
    }
    val generativeProVisionModel by lazy {
        GenerativeModel(
            // For text-only input, use the gemini-pro model
            modelName = "gemini-pro-vision",
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = getString(R.string.apiKey)
        )
    }
    val markdownProcessor = MarkdownProcessor(this).apply {
        factory(TextFactory.create())
    }

    override fun onCreate() {
        DynamicColors.applyToActivitiesIfAvailable(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        isAppInForeGround = true
                    }

                    Lifecycle.Event.ON_STOP -> {
                        isAppInForeGround = false
                    }

                    else -> {}
                }
            }
        })


        super.onCreate()
    }

    fun isAppInForeground() = isAppInForeGround
}
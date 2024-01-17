package com.psb.geminiai.ui.main

import android.content.Intent
import android.os.Bundle
import com.psb.geminiai.databinding.ActivityMainBinding
import com.psb.geminiai.ui.base.BaseActivity
import com.psb.geminiai.ui.chat.ChatActivity

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnNewChat.setOnClickListener {
            startActivity(Intent(activity, ChatActivity::class.java))
        }
    }
}
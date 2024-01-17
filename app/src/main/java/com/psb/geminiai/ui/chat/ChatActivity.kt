package com.psb.geminiai.ui.chat

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.ai.sample.feature.chat.ChatMessage
import com.psb.geminiai.R
import com.psb.geminiai.databinding.ActivityChatBinding
import com.psb.geminiai.ui.base.BaseActivity
import com.psb.geminiai.utils.AppConstants
import com.psb.geminiai.utils.setGone
import com.psb.geminiai.utils.setVisible
import com.yydcdut.markdown.MarkdownProcessor
import com.yydcdut.markdown.syntax.edit.EditFactory
import java.io.File

class ChatActivity : BaseActivity<ChatViewModel, ActivityChatBinding>() {

    override fun getViewBinding() = ActivityChatBinding.inflate(layoutInflater)
    val list = ArrayList<ChatMessage>()
    val adapter by lazy { ChatAdapter(activity, list, appDelegate.markdownProcessor) }
    private var isImagePicked = false
    private var bitmap: Bitmap? = null

    private val resultFilePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { it ->
                val uri: Uri? = it.data
                uri?.let {
                    AppConstants.getPathFromUri(activity, it)?.let { it1 ->
                        viewModel.photoFile = File(it1)
                        loadOrClearImagePreview(BitmapFactory.decodeFile(viewModel.photoFile?.absolutePath))
                    }
                }
            }
        }
    }

    private val resultPhotoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { result ->
        val uri: Uri? = result
        uri?.let {
            AppConstants.getPathFromUri(activity, it)?.let { it1 ->
                viewModel.photoFile = File(it1)
                loadOrClearImagePreview(BitmapFactory.decodeFile(viewModel.photoFile?.absolutePath))
            }
        }

    }

    private val resultCaptureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.getRotatedImage {
                loadOrClearImagePreview(it)
            }
        }
    }

    private fun loadOrClearImagePreview(bitmap: Bitmap? = null) {
        bitmap?.let {
            this.bitmap = bitmap
            Glide.with(activity.applicationContext).asBitmap().load(bitmap).placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background).into(binding.imgPreview)
            binding.rlImgPreview.setVisible()
            isImagePicked = true
        } ?: kotlin.run {
            this.bitmap = null
            binding.rlImgPreview.setGone()
            isImagePicked = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.resultPhotoPicker = resultPhotoPicker
        viewModel.resultCaptureImage = resultCaptureImage
        viewModel.resultFilePicker = resultFilePicker
        viewModel.startChat()

        val markdownProcessor = MarkdownProcessor(this)
        markdownProcessor.factory(EditFactory.create())
        markdownProcessor.live(binding.textMessageInput)

        binding.sendMessageButton.setOnClickListener {
            val string = binding.textMessageInput.text.toString()
            viewModel.sendMessage(string, isImagePicked, bitmap)
            binding.textMessageInput.setText("")
        }
        val manager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        manager.stackFromEnd = true

        binding.rvMessage.layoutManager = manager
        binding.rvMessage.adapter = adapter

        binding.btnPickImg.setOnClickListener {
            viewModel.dialogSelectCameraOrGallery(activity)
        }

        binding.btnClearImg.setOnClickListener{
            loadOrClearImagePreview()
        }

        viewModel.messageObserver.observe(this) {
            if (list.contains(it) && it.isPending) {
                val pos = list.indexOf(it)
                list[pos] = it
                adapter.notifyItemChanged(pos)
                binding.rvMessage.scrollToPosition(pos)
            } else {
                if (!list.contains(it)) {
                    list.add(it)
                    adapter.notifyItemInserted(list.size - 1)
                    binding.rvMessage.scrollToPosition(list.size - 1)
                } else {
                    val pos = list.indexOf(it)
                    list[pos] = it
                    adapter.notifyItemChanged(pos)
                    binding.rvMessage.scrollToPosition(pos)
                }
            }
        }
    }

}
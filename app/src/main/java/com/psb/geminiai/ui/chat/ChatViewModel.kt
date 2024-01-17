package com.psb.geminiai.ui.chat

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.type.ImagePart
import com.google.ai.client.generativeai.type.content
import com.google.ai.sample.feature.chat.ChatMessage
import com.google.ai.sample.feature.chat.Participant
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.permissionx.guolindev.PermissionX
import com.psb.geminiai.ui.base.BaseViewModel
import com.psb.geminiai.utils.AppConstants
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatViewModel : BaseViewModel() {

    private lateinit var chat: Chat
    val messageObserver = MutableLiveData<ChatMessage>()

    lateinit var resultPhotoPicker: ActivityResultLauncher<PickVisualMediaRequest>
    lateinit var resultCaptureImage: ActivityResultLauncher<Intent>
    lateinit var resultFilePicker: ActivityResultLauncher<Intent>
    var photoFile: File? = null

    fun startChat() {
        chat = appDelegate.generativeProModel.startChat(
            history = listOf(),
        )
    }

    fun sendMessage(userInput: String, isImageSelected: Boolean = false, bitmap: Bitmap? = null) {
        ioScope.launch {
            messageObserver.postValue(
                ChatMessage(
                    text = userInput,
                    participant = Participant.USER,
                    img = bitmap,
                )
            )
            val message = ChatMessage(
                text = "",
                participant = Participant.MODEL,
                isPending = false
            )
            if (!isImageSelected) {
                chat.sendMessageStream(userInput).onCompletion {
                    messageObserver.postValue(
                        message.apply {
                            isPending = false
                        }
                    )
                }.collect { chunk ->
                    messageObserver.postValue(
                        message.apply {
                            text += chunk.text
                            isPending = true
                        }
                    )
                }
            } else {
                val content = content {
                    bitmap?.let {part( ImagePart(it)) }
//                    bitmap?.let {part( ImagePart(it)) }
//                    bitmap?.let {part( ImagePart(it)) }
                    text(userInput)
                }
                appDelegate.generativeProVisionModel.generateContentStream(content).onCompletion {
                    messageObserver.postValue(
                        message.apply {
                            isPending = false
                        }
                    )
                }.collect { chunk ->
                    messageObserver.postValue(
                        message.apply {
                            text += chunk.text
                            isPending = true
                        }
                    )
                }
            }
        }
    }

    fun dialogSelectCameraOrGallery(activity: Activity) {
        uiScope.launch {
            val items: Array<CharSequence> = arrayOf("Capture Image", "Choose from Library")

            val builder = MaterialAlertDialogBuilder(activity)
            builder.setItems(items) { dialog: DialogInterface?, item: Int ->
                if (items[item] == "Capture Image") {
                    dialog?.dismiss()
                    checkCameraPermission(activity)
                } else if (items[item] == "Choose from Library") {
                    dialog?.dismiss()
                    checkStoragePermission(activity)
                }
            }
            builder.show()
        }
    }

    private fun checkStoragePermission(activity: Activity) {
        if (!ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(activity)) {
            val permissions = arrayListOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            PermissionX.init(activity as FragmentActivity).permissions(permissions)
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(deniedList, "please allow storage permission to select the profile picture.", "OK", "Cancel")
                }.onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(deniedList, "You need to allow storage permissions in Settings manually", "OK", "Cancel")
                }.request { allGranted, _, _ ->
                    if (allGranted) openFilePickerForImage(activity)
                }
        } else {
            openFilePickerForImage(activity)
        }
    }

    private fun checkCameraPermission(activity: Activity) {
        val permissions = arrayListOf(Manifest.permission.CAMERA)
        PermissionX.init(activity as FragmentActivity).permissions(permissions)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, "please allow camera permission to capture your profile pic", "OK", "Cancel")
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, "You need to allow camera permissions in Settings manually", "OK", "Cancel")
            }.request { allGranted, _, _ ->
                if (allGranted) {
                    photoFile = createImageOrVideoFile(activity)
                    photoFile?.let { openCamera(activity, it) }
                }
            }
    }

    private fun openFilePickerForImage(activity: Activity) {
        if (!ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(activity)) {
            val pickIntent = Intent(Intent.ACTION_PICK)
            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            val chooserIntent = Intent.createChooser(pickIntent, "Select Profile Picture")
            resultFilePicker.launch(chooserIntent)
        } else {
            resultPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun openCamera(activity: Activity, photoFile: File) {
        val photoUri = FileProvider.getUriForFile(activity, AppConstants.fileProvider, photoFile)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        resultCaptureImage.launch(cameraIntent)
    }

    private fun createImageOrVideoFile(activity: Activity): File? {
        var file: File? = null
        try {
            val filename = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            file = File(AppConstants.getCacheDirectoryPath(activity), "$filename.jpg")

            if (!file.parentFile?.exists()!!) {
                file.parentFile?.mkdirs()
            }
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
        } catch (e: Exception) {
            Log.d("TAG", "CreateImageFile: " + e.localizedMessage)
        }
        return file
    }

    fun getRotatedImage(onBitmapGeneration: (bitmap: Bitmap) -> Unit) {
        uiScope.launch {
            var ei: ExifInterface? = null
            try {
                ei = photoFile?.let { ExifInterface(it.absolutePath) }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val orientation = ei?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
            val bitmap = BitmapFactory.decodeFile(photoFile?.absolutePath)
            val rotatedBitmap: Bitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
                ExifInterface.ORIENTATION_NORMAL -> bitmap
                else -> bitmap
            }
            onBitmapGeneration(rotatedBitmap)
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}
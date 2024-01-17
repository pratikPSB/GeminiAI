package com.psb.geminiai.utils

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.psb.geminiai.BuildConfig
import com.psb.geminiai.R
import com.psb.geminiai.service.AppDelegate
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.ln


object AppConstants {

    const val extraKeyPhone = "PHONE"
    const val extraKeyFromCreateCircleScreen = "FromCreateCircleScreen"
    const val extraKeyActivityName = "ActivityName"
    const val extraKeyComeFrom = "ComeFrom"
    const val extraKeyMembersModel = "membersModel"

    const val noInternet = "No Internet"

    const val fileProvider = "${BuildConfig.APPLICATION_ID}.fileProvider"

    var FCM_TOKEN = ""
    const val deviceType = 301
    const val commonContentType = "application/json"
    val commonContentTypeForRequestBody = "application/json; charset=utf-8".toMediaTypeOrNull()

    /*fun showProgressDialog(context: Activity): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val inflater = context.layoutInflater
        val view: View = inflater.inflate(R.layout.progress_dialog, null)
        builder.setView(view)
        val dialog: Dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }*/

    fun hideKeyboard(context: Context?) {
        if (context is Activity) {
            val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = context.currentFocus
            if (view == null) {
                view = View(context)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun setWhiteNavigationBar(dialog: Dialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    fun getBitmapFromView(view: View): Bitmap {
        return if (view.measuredHeight <= 0) {
            view.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val b = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            view.draw(c)
            b
        } else {
            val b = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            view.draw(c)
            b
        }
    }

    fun shareToAllAppOrSpecificApp(activity: Activity, message: String = "https://play.google.com/store/apps/details?id=${activity.packageName}", shareOnSpecificApp: Boolean = false, packageName: String = "") {
        try {
            val waIntent = Intent(Intent.ACTION_SEND)
            waIntent.type = "text/plain"
            waIntent.putExtra(Intent.EXTRA_TEXT, message)
            if (shareOnSpecificApp) {
                if (packageName.isEmpty()) {
                    throw Exception("If you want to share on specific app then you have to put package name also")
                }
                if (appInstalledOrNot(activity, packageName)) {
                    waIntent.setPackage(packageName)
                } else {
                    activity.showShortToast("WhatsApp Not Installed")
                    return
                }
                activity.startActivity(Intent.createChooser(waIntent, "share"))
            } else {
                activity.startActivity(Intent.createChooser(waIntent, "share"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            activity.showShortToast("WhatsApp Not Installed")
        }
    }

    private fun appInstalledOrNot(activity: Activity, uri: String?): Boolean {
        val pm = activity.packageManager
        try {
            pm.getPackageInfo(uri!!, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    @SuppressLint("HardwareIds")
    fun getDeviceID(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getDeviceVersion(): String {
        return Build.VERSION.SDK_INT.toString()
    }

    /*fun isUserLoggedIn(appDelegate: AppDelegate) = !getToken(Preferences(appDelegate.applicationContext)).isNullOrEmpty() && appDelegate.firebaseLoginUtils()
        .getCurrentUser() != null*/

    fun openBrowser(activity: Activity, link: String? = "") {
        var finalLink = link
        if (finalLink == "") {
            finalLink = "https://www.google.com"
        }
        val packageName = "com.android.chrome"
        val isAppInstalled: Boolean = appInstalledOrNot(activity, packageName)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalLink))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (isAppInstalled) intent.setPackage("com.android.chrome")
        try {
            activity.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            intent.setPackage(null)
            activity.startActivity(intent)
        }
    }

    private fun clearPreference(activity: Context) {
        Preferences(activity).clearData()
    }

    private const val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,15}$"
    private val pattern = Pattern.compile(PASSWORD_PATTERN)
    const val passwordError = "Password Invalid.\nEnter a combination of 8 characters at least 1 uppercase alphabet, 1 lowercase alphabet, 1 number, and 1 special character."
    fun isPasswordValid(password: String): Boolean {
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }

    /*fun buildFirebaseDeepLink(activity: Activity, circleCode: String? = null) {
        val preferences = Preferences(activity)
        *//*val dynamicLink = *//*FirebaseDynamicLinks.getInstance()
            .createDynamicLink()
            .setLink(
                Uri.parse(
                    "https://f1fmj8vx.page.link/?circle_id=${circleCode ?: kotlin.run { preferences.getInt(pref_selected_circle_id) }}&circle_code=${preferences.getInt(pref_circle_code)}&circle_name=${preferences.getString(pref_circle_name)}"
                )
            )
            .setDomainUriPrefix("https://f1fmj8vx.page.link/")
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder(activity.packageName)
                    .setFallbackUrl(Uri.parse("https://play.google.com/store/apps/details?id=${activity.packageName}&hl=en_IN&gl=US"))
                    .build()
            )
            .setIosParameters(
                DynamicLink.IosParameters.Builder(activity.packageName)
                    .setFallbackUrl(Uri.parse("https://play.google.com/store/apps/details?id=${activity.packageName}&hl=en_IN&gl=US"))
                    .setAppStoreId("123456789")
                    .build()
            )*//*.buildDynamicLink().uri
        Log.d(TAG, "buildFirebaseDeepLink: $dynamicLink")*//*
            .buildShortDynamicLink()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    preferences.setString(pref_deep_link, it.result.shortLink.toString())
                }
            }
    }*/

    private fun getZoomLevel(radius: Double): Float {
        val zoomLevel: Float
        val scale = radius / 200
        zoomLevel = (16 - ln(scale) / ln(2.0)).toFloat()
        return zoomLevel + .5f
    }

    private const val NOTIFICATION_CHANNEL_ID: String = BuildConfig.APPLICATION_ID + ".channel"

    fun sendNotification(context: Context, title: String?, message: String?, pendingIntent: PendingIntent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            val name = context.getString(R.string.app_name)
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setTicker("Family Locator")
            .setSmallIcon(R.drawable.iv_message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setOnlyAlertOnce(true)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(getUniqueId(), notification)
    }

    fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())

    fun getCacheDirectoryPath(activity: Activity): String {
        return activity.cacheDir.toString() + "/GeminiAI"
    }

    fun getPathFromUri(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= 23
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }

                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }

                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    fun showNoNetworkDialog(activity: Activity) {
        if ((activity.application as AppDelegate).isAppInForeground())
            AlertDialog
                .Builder(activity)
                .setTitle("No Internet")
                .setMessage("No Internet Available. Please check your internet connectivity.")
                .setPositiveButton("OK") { dialogInterface, i ->
                    dialogInterface.dismiss()
                }.show()
    }
}
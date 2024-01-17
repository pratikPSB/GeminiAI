package com.psb.geminiai.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.psb.geminiai.BuildConfig
import com.psb.geminiai.R
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

const val TAG = "PSB"

fun <T : Any?> Activity.startActivity(className: Class<T>, isFinish: Boolean = false, isClearTop: Boolean = false) {
    val intent = Intent(this, className)
    if (isClearTop) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
    if (isFinish) {
        finish()
    }
}

fun <T : Any?> Activity.startActivitySlideIn(className: Class<T>) {
    val intent = Intent(this, className)
    startActivity(intent)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_down, R.anim.slide_out_up)
    } else {
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up)
    }
}

fun Activity.finishActivityWithSlideOut() {
    finish()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.slide_in_up, R.anim.slide_out_down)
    } else {
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down)
    }
}

fun <T : Any?> Activity.activityResultLauncherWithSlideIn(className: Class<T>, launcher: ActivityResultLauncher<Intent>) {
    val intent = Intent(this, className)
    launcher.launch(intent)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_down, R.anim.slide_out_up)
    } else {
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up)
    }
}

val Number.toPx
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

private fun round(unrounded: Double, precision: Int, roundingMode: Int): Double {
    val bd = BigDecimal(unrounded)
    val rounded = bd.setScale(precision, roundingMode)
    return rounded.toDouble()
}

fun Number.roundedDouble(): Double {
    val decimalFormat = DecimalFormat("0.0000")
    return decimalFormat.format(this).toDouble()
}

fun View.showKeyboard() {
    this.requestFocus()
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(this, 0)
}

fun View.hideKeyboard() {
    this.clearFocus()
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.setVisible() {
    this.visibility = View.VISIBLE
}

fun View.setGone() {
    this.visibility = View.GONE
}

fun View.setInvisible() {
    this.visibility = View.INVISIBLE
}

fun View.setMargins(isDefaultMargin: Boolean = false, leftMargin: Int = 0, topMargin: Int = 0, rightMargin: Int = 0, bottomMargin: Int = 0, convertToPX: Boolean = true) {
    val param = this.layoutParams as ViewGroup.MarginLayoutParams
    if (isDefaultMargin) {
        param.leftMargin = 10.toPx.toInt()
        param.topMargin = 10.toPx.toInt()
        param.rightMargin = 10.toPx.toInt()
        param.bottomMargin = 10.toPx.toInt()
    } else {
        param.leftMargin = if (convertToPX) leftMargin.toPx.toInt() else leftMargin
        param.topMargin = if (convertToPX) topMargin.toPx.toInt() else topMargin
        param.rightMargin = if (convertToPX) rightMargin.toPx.toInt() else rightMargin
        param.bottomMargin = if (convertToPX) bottomMargin.toPx.toInt() else bottomMargin
    }
    this.layoutParams = param
    this.invalidate()
}

fun EditText.afterTextChanged(afterTextChange: AfterTextChange) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            afterTextChange.onReceiveText(s.toString())
        }
    })
}

interface AfterTextChange {
    fun onReceiveText(text: String)
}

fun Activity.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Fragment.showShortToast(message: String) {
    Toast.makeText(this.requireActivity(), message, Toast.LENGTH_SHORT).show()
}

fun Fragment.showLongToast(message: String) {
    Toast.makeText(this.requireActivity(), message, Toast.LENGTH_LONG).show()
}

fun showSnackBar(message: String, rootView: View) {
    val snack = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
    val view = snack.view
    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
        maxLines = 5
    }
    snack.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
    snack.show()
}

fun View.getNonZeroMeasuredHeight(): Int {
    if (measuredHeight <= 0) {
        measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    return measuredHeight
}

fun AppCompatImageView.loadImageFromGlide(imagePath: Any?, placeHolder: Int = R.mipmap.ic_launcher_round) {
    Glide.with(this.context).load(imagePath).diskCacheStrategy(DiskCacheStrategy.ALL)
        .placeholder(placeHolder).error(placeHolder).into(this)
}

fun ImageView.loadImageFromGlide(imagePath: Any?, placeHolder: Int = R.mipmap.ic_launcher_round) {
    Glide.with(this.context).load(imagePath).diskCacheStrategy(DiskCacheStrategy.ALL)
        .placeholder(placeHolder).error(placeHolder).into(this)
}

fun Activity.enableActivity(isEnabled: Boolean) {
    if (!isEnabled) {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}

fun Long.timeAgo(): String {
    val SECOND_MILLIS = 1000
    val MINUTE_MILLIS = 60 * SECOND_MILLIS
    val HOUR_MILLIS = 60 * MINUTE_MILLIS
    val DAY_MILLIS = 24 * HOUR_MILLIS

    val now = System.currentTimeMillis()
    if (this > now || this <= 0) {
        return convertTimeStampToDate(this)
    }

    val diff: Long = now - this

    val hour = diff / HOUR_MILLIS
    val minute = diff / MINUTE_MILLIS
    val second = diff / SECOND_MILLIS

    return if (diff < MINUTE_MILLIS) {
        "$second seconds ago"
    } else if (diff < 60 * MINUTE_MILLIS) {
        if (minute == 1L)
            "$minute minute ago"
        else
            "$minute minutes ago"
    } else if (diff < 24 * HOUR_MILLIS) {
        if (hour == 1L)
            "$hour hour ago"
        else
            "$hour hours ago"
    } else if (diff < 48 * HOUR_MILLIS) {
        "Yesterday";
    } else {
        convertTimeStampToDate(this)
    }
}

fun convertTimeStampToDate(timestamp: Long): String {
    val calendar = Calendar.getInstance(Locale.ENGLISH)
    calendar.timeInMillis = timestamp
    val date = DateFormat.format("MMM dd, yyyy", calendar).toString()
    return date
}

fun log(msg: String) {
    if (BuildConfig.DEBUG) {
        Log.d(TAG, "log: $msg")
    }
}
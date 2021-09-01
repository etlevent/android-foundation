@file:JvmName("ComponentUtils")

package ext.android.foundation.extensions

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.AnyThread
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import ext.android.foundation.util.Drawables
import java.io.File
import java.util.concurrent.Executor
import kotlin.math.roundToInt

class InternalRes {
    companion object {
        const val STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height"
        const val NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height"
    }
}

val Context.mainThreadExecutor: Executor
    @SuppressLint("RestrictedApi")
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) mainExecutor else ArchTaskExecutor.getMainThreadExecutor()

private fun isMainThread() = Looper.getMainLooper() == Looper.myLooper()

fun Context.isMainProcess(): Boolean {
    val pid = android.os.Process.myPid()
    val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (appProcess in am.runningAppProcesses) {
        if (appProcess.pid == pid) {
            return applicationInfo.packageName == appProcess.processName
        }
    }
    return false
}

fun Context.isApplicationInBackground(): Boolean {
    val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (runningTask in am.getRunningTasks(1)) {
        val topActivity = runningTask.topActivity ?: continue
        if (topActivity.packageName == packageName) return true
    }
    return false
}

@SuppressLint("ShowToast")
@JvmOverloads
@AnyThread
fun Context.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    val runnable = Runnable { Toast.makeText(this, text, duration).show() }
    when {
        isMainThread() -> runnable.run()
        else -> mainThreadExecutor.execute(runnable)
    }
}

@JvmOverloads
@AnyThread
fun Fragment.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    context!!.showToast(text, duration)
}

fun Context.getInternalDimensionSize(key: String): Int {
    var result = 0
    try {
        val resourceId = resources.getIdentifier(key, "dimen", "android")
        if (resourceId > 0) {
            result = (resources.getDimensionPixelSize(resourceId) *
                    Resources.getSystem().displayMetrics.density /
                    resources.displayMetrics.density).roundToInt()
        }
    } catch (ignored: Resources.NotFoundException) {
        return 0
    }
    return result
}

val Context.statusBarHeight: Int
    get() = getInternalDimensionSize(InternalRes.STATUS_BAR_HEIGHT_RES_NAME)

val Context.navBarHeight: Int
    get() = getInternalDimensionSize(InternalRes.NAV_BAR_HEIGHT_RES_NAME)

val Context.realDisplayMetrics: DisplayMetrics
    @SuppressLint("ObsoleteSdkInt")
    get() {
        val windowManager: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return DisplayMetrics().also { metrics ->
            val display: Display = windowManager.defaultDisplay
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics)
            } else {
                try {
                    Display::class.java.getDeclaredMethod(
                        "getRealMetrics",
                        DisplayMetrics::class.java
                    ).invoke(display, metrics)
                } catch (e: Exception) {
                    e.printStackTrace()
                    display.getMetrics(metrics)
                }
            }
        }
    }

fun Context.copyAssets(path: String, dstDir: File) {
    val children = assets.list(path) ?: return
    if (children.isNotEmpty()) {
        children.map { "$path/$it" }
            .forEach { copyAssets(it, dstDir) }
    } else {
        File(dstDir, path)
            .also {
                if (it.parentFile?.exists() == false)
                    it.parentFile?.mkdirs()
            }
            .outputStream().use { os ->
                assets.open(path).use { `is` ->
                    os.write(`is`.readBytes())
                }
            }
    }
}

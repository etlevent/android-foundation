package ext.android.foundation.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

object AppCompat {
    @JvmStatic
    fun createCaptureIntent(context: Context, file: File): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri: Uri = createCompatUri(context, file)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        return intent
    }

    @JvmStatic
    fun createCropIntent(context: Context, uri: Uri): Intent {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            grantUriPermissions(context, intent, uri)
        }
        // 设置裁剪
        intent.putExtra("crop", "true")
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 320)
        intent.putExtra("outputY", 320)
        intent.putExtra("return-data", true)
        return intent
    }

    @JvmStatic
    fun createCompatUri(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createFileProviderUri(context, file)
        } else {
            Uri.fromFile(file)
        }
    }

    private const val AUTHORITIES_SUFFIX: String = ".FILE_PROVIDER"

    /**
     * uri转化成适应7.0的content://形式 针对图片格式
     */
    @JvmStatic
    fun grantUriPermissions(context: Context, intent: Intent, file: File): Intent {
        val uri =
            FileProvider.getUriForFile(context, context.packageName + AUTHORITIES_SUFFIX, file)
        val resolveInfoList =
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resolveInfoList) {
            context.grantUriPermission(
                resolveInfo.activityInfo.packageName, uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return intent
    }

    /**
     * 对content://请求临时授权
     */
    @JvmStatic
    fun grantUriPermissions(context: Context, intent: Intent, uri: Uri): Intent {
        val resolveInfoList =
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resolveInfoList) {
            context.grantUriPermission(
                resolveInfo.activityInfo.packageName, uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return intent
    }

    /**
     * 普通uri转化成适应7.0的content://形式
     */
    @JvmStatic
    fun createFileProviderUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, context.packageName + AUTHORITIES_SUFFIX, file)
    }
}
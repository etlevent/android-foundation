package ext.android.foundation.core

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.annotation.Size
import androidx.fragment.app.Fragment
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

typealias PermissionResultCallback = (Map<String, Int>) -> Unit
typealias ActivityResultCallback = (Intent?) -> Unit

class AndroidResultDispatchFragment internal constructor() : Fragment() {
    companion object {
        private const val REQUEST_CODE = 100
    }

    private val currentRequestCode = AtomicInteger(REQUEST_CODE)
    private val activityResultCallbacks: ConcurrentHashMap<Int, ActivityResultCallback> =
        ConcurrentHashMap()
    private val permissionResultCallbacks: ConcurrentHashMap<Int, PermissionResultCallback> =
        ConcurrentHashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun startActivityForResult(intent: Intent, callback: ActivityResultCallback) {
        val requestCode = currentRequestCode.getAndIncrement()
        activityResultCallbacks[requestCode] = callback
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        val callback = activityResultCallbacks[requestCode] ?: return
        callback(data)
        if (currentRequestCode.get() == requestCode)
            currentRequestCode.decrementAndGet()
        activityResultCallbacks.remove(requestCode)
    }

    fun requestPermissions(
        @Size(min = 1) permissions: Array<out String>,
        callback: PermissionResultCallback
    ) {
        val requestCode = currentRequestCode.getAndIncrement()
        permissionResultCallbacks[requestCode] = callback
        requestPermissions(permissions, requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val callback = permissionResultCallbacks[requestCode] ?: return
        grantResults.map { index ->
            val permission = permissions[index]
            val grantResult = grantResults[index]
            val permissionResult = when {
                grantResult == PackageManager.PERMISSION_GRANTED -> PermissionResult.GRANTED
                shouldShowRequestPermissionRationale(permission) -> PermissionResult.DENIED
                grantResult == PackageManager.PERMISSION_DENIED -> PermissionResult.PERMANENTLY_DENIED
                else -> PermissionResult.UNKNOWN
            }
            permission to permissionResult
        }.toMap().let { callback(it) }
        if (currentRequestCode.get() == requestCode)
            currentRequestCode.decrementAndGet()
        permissionResultCallbacks.remove(requestCode)
    }
}
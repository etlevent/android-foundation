package ext.android.foundation.core

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AndroidResults private constructor(fragmentManager: FragmentManager) {

    private val dispatchFragment: AndroidResultDispatchFragment by lazy {
        val fragment = fragmentManager.findFragmentByTag(REQUEST_ANDROID_RESULT_FRAGMENT_TAG)
            ?: AndroidResultDispatchFragment().apply {
                fragmentManager.beginTransaction()
                    .add(this, REQUEST_ANDROID_RESULT_FRAGMENT_TAG)
                    .commitNow()
            }
        if (fragment !is AndroidResultDispatchFragment)
            throw RuntimeException("AndroidResults should add with tag [$REQUEST_ANDROID_RESULT_FRAGMENT_TAG]")
        fragment
    }

    fun startActivityForResult(intent: Intent, callback: ActivityResultCallback) {
        dispatchFragment.startActivityForResult(intent, callback)
    }

    fun startActivityForResult(intent: Intent): LiveData<Intent> {
        val result = MutableLiveData<Intent>()
        dispatchFragment.startActivityForResult(intent) { result.value = it }
        return result
    }

    fun requestPermissions(@Size(min = 1) vararg permissions: String, callback: PermissionResultCallback) {
        dispatchFragment.requestPermissions(permissions, callback)
    }

    fun requestPermissions(@Size(min = 1) vararg permissions: String): LiveData<Map<String, Int>> {
        val result: MutableLiveData<Map<String, Int>> = MutableLiveData()
        dispatchFragment.requestPermissions(permissions) { result.value = it }
        return result
    }

    fun hasPermissions(@Size(min = 1) vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val context = dispatchFragment.requireContext()
        return permissions.any { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private const val REQUEST_ANDROID_RESULT_FRAGMENT_TAG = "android.fragment.dispatch_result"

        @JvmStatic
        fun with(activity: AppCompatActivity) = AndroidResults(activity.supportFragmentManager)

        @JvmStatic
        fun with(fragment: Fragment) = AndroidResults(fragment.childFragmentManager)
    }
}



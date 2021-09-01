package ext.android.foundation.core

import androidx.annotation.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData

fun AppCompatActivity.requestPermissions(@Size(min = 1) vararg permissions: String): LiveData<Map<String, Int>> =
    AndroidResults.with(this).requestPermissions(*permissions)

fun Fragment.requestPermissions(@Size(min = 1) vararg permissions: String): LiveData<Map<String, Int>> =
    AndroidResults.with(this).requestPermissions(*permissions)

fun AppCompatActivity.hasPermissions(@Size(min = 1) vararg permissions: String) =
    AndroidResults.with(this).hasPermissions(*permissions)

fun Fragment.hasPermissions(@Size(min = 1) vararg permissions: String) =
    AndroidResults.with(this).hasPermissions(*permissions)
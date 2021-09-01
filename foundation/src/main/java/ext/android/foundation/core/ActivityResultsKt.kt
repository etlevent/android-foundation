package ext.android.foundation.core

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData

fun AppCompatActivity.startActivityForResult(intent: Intent): LiveData<Intent> =
    AndroidResults.with(this).startActivityForResult(intent)

fun Fragment.startActivityForResult(intent: Intent): LiveData<Intent> =
    AndroidResults.with(this).startActivityForResult(intent)
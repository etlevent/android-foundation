package ext.android.foundation.core

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.LOCAL_VARIABLE
)
@IntDef(PermissionResult.GRANTED, PermissionResult.DENIED, PermissionResult.PERMANENTLY_DENIED)
annotation class PermissionResult {
    companion object {
        const val UNKNOWN = -1
        const val GRANTED = 0
        const val DENIED = 1
        const val PERMANENTLY_DENIED = 2
    }
}
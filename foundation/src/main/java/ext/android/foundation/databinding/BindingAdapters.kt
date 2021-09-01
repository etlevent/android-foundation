package ext.android.foundation.databinding

import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import ext.android.foundation.util.Drawables

@BindingMethods(
    BindingMethod(
        type = View::class,
        attribute = "android:selected",
        method = "setSelected"
    ),
    BindingMethod(
        type = View::class,
        attribute = "android:enabled",
        method = "setEnable"
    ),
    BindingMethod(
        type = CheckBox::class,
        attribute = "android:button",
        method = "setButtonDrawable"
    )
)
object BindingAdapters {

    @JvmStatic
    @BindingAdapter("backgroundTint")
    fun setBackgroundTint(view: View, stateList: ColorStateList) {
        ViewCompat.setBackgroundTintList(view, stateList)
    }

    @JvmStatic
    @BindingAdapter("tint")
    fun setImageTint(imageView: ImageView, stateList: ColorStateList) {
        ImageViewCompat.setImageTintList(imageView, stateList)
    }

    @JvmStatic
    @BindingAdapter("drawableTint")
    fun setDrawableTint(view: TextView, stateList: ColorStateList) {
        TextViewCompat.setCompoundDrawableTintList(view, stateList)
    }
}
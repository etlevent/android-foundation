package ext.android.foundation.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

object Drawables {
    @JvmStatic
    fun tint(context: Context, @DrawableRes id: Int, @ColorInt tint: Int): Drawable {
        val drawable = ContextCompat.getDrawable(context, id)
        return tint(drawable!!, tint)
    }

    @JvmStatic
    fun tint(drawable: Drawable, @ColorInt tint: Int): Drawable {
        drawable.mutate()
        val wrap = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(wrap, tint)
        return wrap
    }
}

fun Context.tint(@DrawableRes id: Int, @ColorInt tint: Int): Drawable =
    Drawables.tint(this, id, tint)
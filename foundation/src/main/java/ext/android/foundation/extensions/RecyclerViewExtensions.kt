//@file:JvmName("RecyclerViewHelper")
//
//package ext.android.foundation.extensions
//
//import android.graphics.drawable.Drawable
//import androidx.annotation.DrawableRes
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//
//fun RecyclerView.addDivider(@DrawableRes dividerId: Int) {
//    ContextCompat.getDrawable(context, dividerId)?.let { addDivider(it) }
//}
//
//fun RecyclerView.addDivider(divider: Drawable) {
//    val decoration = DividerItemDecoration(context)
//    decoration.setDrawable(divider)
//    addItemDecoration(decoration)
//}

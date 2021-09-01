package ext.android.foundation.ui.widget

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.ColorInt
import ext.android.foundation.extensions.statusBarHeight

class KeyboardFloatingWindow private constructor(private val builder: Builder) :
    ViewTreeObserver.OnGlobalLayoutListener {

    private val mWindow: Window = builder.window
    private val mWindowSoftInputMode: Int = mWindow.attributes.softInputMode
    private val mChildOfContent: View
    private val mChildLayoutParams: ViewGroup.MarginLayoutParams
    private val mWindowOutRect = Rect()
    private var mInitUsableHeight: Int = 0
    private var mIsKeyboardShowing: Boolean = false
    private val mShortcuts: ArrayList<Shortcut> = builder.shortcuts

    private val mOnKeyboardChangedListeners = ArrayList<OnKeyboardChangedListener>()

    private val mPopupWindow: PopupWindow by lazy {
        PopupWindow(
            mPopupContentView,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            isOutsideTouchable = false
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
    private val mPopupContentView: LinearLayout by lazy {
        LinearLayout(mWindow.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setPadding(8, 8, 8, 8) }
            setBackgroundColor(builder.backgroundColor)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
        }.apply {
            mShortcuts.forEach { shortcut ->
                TextView(mWindow.context).also { textView ->
                    textView.text = shortcut.text
                    textView.setTextColor(shortcut.textColor)
                    textView.setPadding(12, 8, 12, 8)
                    textView.setOnClickListener {
                        shortcut.action?.run()
                    }
                }.let { textView ->
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    addView(textView, params)
                }
            }
        }
    }

    private val mPopupContentHeight: Int by lazy {
        if (mPopupContentView.measuredHeight == 0) {
            mPopupContentView.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )
        }
        mPopupContentView.measuredHeight
    }

    private val mStatusBarHeight: Int by lazy { mWindow.context.statusBarHeight }

    init {
        val contentParent: ViewGroup = mWindow.decorView.findViewById(Window.ID_ANDROID_CONTENT)
        mChildOfContent = contentParent.getChildAt(0)
        mChildLayoutParams = (mChildOfContent.layoutParams as ViewGroup.MarginLayoutParams?)
            ?: ViewGroup.MarginLayoutParams(mChildOfContent.layoutParams)
                .also { params -> mChildOfContent.layoutParams = params }
    }

    fun addOnKeyboardChangedListener(listener: OnKeyboardChangedListener) {
        if (!mOnKeyboardChangedListeners.contains(listener))
            mOnKeyboardChangedListeners.add(listener)
    }

    fun removeOnKeyboardChangedListener(listener: OnKeyboardChangedListener) {
        mOnKeyboardChangedListeners.remove(listener)
    }

    fun enable() {
        if (mWindowSoftInputMode and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST != WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            || mWindowSoftInputMode and WindowManager.LayoutParams.SOFT_INPUT_MASK_STATE != WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )
            mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        mChildOfContent.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    @SuppressLint("ObsoleteSdkInt")
    fun disable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mChildOfContent.viewTreeObserver.removeOnGlobalLayoutListener(this)
        } else {
            mChildOfContent.viewTreeObserver.removeGlobalOnLayoutListener(this)
        }
        mWindow.setSoftInputMode(mWindowSoftInputMode)
    }

    private fun computeUsableHeight(): Int {
        mWindow.decorView.getWindowVisibleDisplayFrame(mWindowOutRect)
        return mWindowOutRect.bottom - mWindowOutRect.top - mWindow.attributes.y
    }

    private fun handleKeyboardChanged(isKeyboardShowing: Boolean, visibleHeight: Int) {
        if (isKeyboardShowing) {
            val availableHeight = visibleHeight + mStatusBarHeight - mPopupContentHeight
//            mChildLayoutParams.height = availableHeight
            offsetChildOfContent(mPopupContentHeight)
            mPopupWindow.showAtLocation(
                mChildOfContent,
                Gravity.START or Gravity.TOP,
                0,
                availableHeight
            )
        } else {
            offsetChildOfContent(-mPopupContentHeight)
            // mChildLayoutParams.height = visibleHeight + mStatusBarHeight
            mPopupWindow.dismiss()
        }
        // mChildOfContent.requestLayout()
    }

    private fun offsetChildOfContent(bottom: Int) {
        mChildLayoutParams.bottomMargin += bottom
        mChildOfContent.layoutParams = mChildLayoutParams
    }

    private fun dispatchKeyboardChanged(isKeyboardShowing: Boolean) {
        mOnKeyboardChangedListeners.forEach { listener ->
            listener.onKeyboardChanged(
                isKeyboardShowing
            )
        }
    }

    override fun onGlobalLayout() {
        val usableHeight: Int = computeUsableHeight()
        if (mInitUsableHeight == 0)
            mInitUsableHeight = usableHeight
        val heightDifference = mInitUsableHeight - usableHeight


        val isKeyboardShowing: Boolean = heightDifference > mInitUsableHeight / 3
        if (mIsKeyboardShowing != isKeyboardShowing) {
            handleKeyboardChanged(isKeyboardShowing, usableHeight)
            mIsKeyboardShowing = isKeyboardShowing
            dispatchKeyboardChanged(mIsKeyboardShowing)
        }
    }

    interface OnKeyboardChangedListener {
        fun onKeyboardChanged(isKeyboardShowing: Boolean)
    }

    data class Shortcut @JvmOverloads constructor(
        val text: String,
        @ColorInt
        val textColor: Int,
        val action: Runnable? = null,
    ) {
        constructor(
            text: String,
            action: Runnable?
        ) : this(text, Color.BLACK, action = action)
    }

    class Builder(val window: Window) {
        @ColorInt
        var backgroundColor: Int = Color.WHITE
            private set
        val shortcuts: ArrayList<Shortcut> = arrayListOf()

        fun backgroundColor(@ColorInt backgroundColor: Int) = apply {
            this.backgroundColor = backgroundColor
        }

        fun addShortcut(shortcut: Shortcut) = apply {
            if (!shortcuts.contains(shortcut))
                shortcuts.add(shortcut)
        }

        fun build() = KeyboardFloatingWindow(this)
    }
}

private class RequestFocusManager(private vararg val views: View) {
    private var currentFocusIndex = -1

    init {
        if (currentFocusIndex != -1 && currentFocusIndex < views.size) {
            views[currentFocusIndex].requestFocus()
        }
    }

    fun requestPrev() {
        if (currentFocusIndex <= 0)
            return
        views[currentFocusIndex].clearFocus()
        currentFocusIndex--
        handleFocusChange(views[currentFocusIndex])
    }

    fun requestNext() {
        if (currentFocusIndex >= views.size - 1)
            return
        views[currentFocusIndex].clearFocus()
        currentFocusIndex++
        handleFocusChange(views[currentFocusIndex])
    }

    private fun handleFocusChange(viewToFocused: View) {
        viewToFocused.requestFocus()
        if (viewToFocused is EditText) {
            val text = viewToFocused.text
            viewToFocused.setSelection(text.length)
        }
    }
}

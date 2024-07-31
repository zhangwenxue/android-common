package android.boot.common.extensions

import android.content.res.Resources
import android.util.TypedValue
import androidx.core.util.TypedValueCompat

val resources by lazy { Resources.getSystem() }
val displayMetrics by lazy { resources.displayMetrics }
val densityDpi by lazy { displayMetrics.density }


val Int.dp: Int
    get() = TypedValueCompat.pxToDp(this.toFloat(), displayMetrics).toInt()
val Int.px: Int
    get() = TypedValueCompat.dpToPx(this.toFloat(), displayMetrics).toInt()

// Px to Mm
val Float.pxAsMm: Float
    get() = (this / densityDpi) * 25.4f

// Mm to Px
val Float.mmAsPx: Float
    get() = TypedValueCompat.deriveDimension(TypedValue.COMPLEX_UNIT_MM, this, displayMetrics)

val Float.dpAsPx: Float
    get() = TypedValueCompat.dpToPx(this, displayMetrics)

val Float.pxAsDp: Float
    get() = TypedValueCompat.pxToDp(this, displayMetrics)

val Float.pxAsSp: Float
    get() = TypedValueCompat.pxToSp(this, displayMetrics)

val Float.spAsPx: Float
    get() = TypedValueCompat.spToPx(this, displayMetrics)
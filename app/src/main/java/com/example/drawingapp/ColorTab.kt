package com.example.drawingapp

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils

class ColorTab(vararg buttons: Button)
{
    public var colors: ArrayList<Button>? = null
    private var alphaValue = 100

    init {
        colors = ArrayList()
        colors!!.addAll(buttons)
    }

    public fun select(button: Button)
    {
        for (but in colors!!) {
            but.background.alpha = 255
            but.scaleX = 1f
            but.scaleY = 1f
            but.clearAnimation()
            if(but.background is GradientDrawable) {
                but.background = ColorDrawable((but.background as GradientDrawable).color!!.defaultColor)
            }

            if (but == button) {
                but.background.alpha = alphaValue

                val scale = ScaleAnimation(1.0f, 0.85f, 1.0f, 0.85f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f)
                scale.duration = 200
                scale.fillAfter = true

                but.startAnimation(scale)

                val border = GradientDrawable()

                val buttonColor = (but.background as ColorDrawable).color
                val colorWithOpacity = ColorUtils.setAlphaComponent(buttonColor, 255)
                border.color = ColorStateList.valueOf(colorWithOpacity)

                border.setStroke(4,
                    if (border.color?.defaultColor != Color.BLACK) Color.BLACK else Color.YELLOW)
                but.background = border
            }
        }
    }

    public fun getSelectedColor(): Button?
    {
        for (but in colors!!) {
            if (but.background.alpha == alphaValue) {
                return but
            }
        }

        return null
    }
}
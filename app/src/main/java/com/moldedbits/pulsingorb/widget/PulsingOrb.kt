package com.moldedbits.pulsingorb.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View


class PulsingOrb : View {

    val defaultColor: Int = Color.parseColor("#F39F83")

    private var minRadius: Float = 20f
        set(value) {
            field = value
            updateSize()
        }

    private var maxRadius: Float = 80f

    private var enableNoise: Boolean = true

    private val paddingFactor: Float = 0.9f

    var noiseX: RangedValue = RangedValue(Vector(- maxRadius * (1 - paddingFactor),
            maxRadius * (1 - paddingFactor)), easing = 0.1f, randomize = true)

    var noiseY: RangedValue = RangedValue(Vector(- maxRadius * (1 - paddingFactor),
            maxRadius * (1 - paddingFactor)), easing = 0.1f, randomize = true)

    val orb1: Pulse = Pulse(Vector(minRadius, maxRadius / 2 * paddingFactor),
            Vector(0f, 0f), 255, defaultColor)
    val orb2: Pulse = Pulse(Vector(minRadius, maxRadius * 6 / 8 * paddingFactor),
            Vector(0f, 0f), 200, defaultColor)
    val orb3: Pulse = Pulse(Vector(minRadius, maxRadius * 7 / 8 * paddingFactor),
            Vector(0f, 0f), 150, defaultColor)
    val orb4: Pulse = Pulse(Vector(minRadius, maxRadius * paddingFactor),
            Vector(0f, 0f), 75, defaultColor)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(
                attrs,
                intArrayOf(android.R.attr.color),
                0, 0)

        try {
            val color = a.getColor(0, defaultColor)

            orb1.setColor(color)
            orb2.setColor(color)
            orb3.setColor(color)
            orb4.setColor(color)
        } finally {
            a.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(animator)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(animator)
        super.onDetachedFromWindow()
    }

    val animator: Runnable = object : Runnable {
        override fun run() {
            invalidate()
            postDelayed(this, 10)
        }
    }

    private fun updateSize() {
        orb1.sizeRange.x = minRadius
        orb2.sizeRange.x = minRadius
        orb3.sizeRange.x = minRadius
        orb4.sizeRange.x = minRadius

        orb1.sizeRange.y = maxRadius / 2 * paddingFactor
        orb2.sizeRange.y = maxRadius * 6 / 8 * paddingFactor
        orb3.sizeRange.y = maxRadius * 7 / 8 * paddingFactor
        orb4.sizeRange.y = maxRadius * paddingFactor
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val centerX = (measuredWidth / 2).toFloat()
        val centerY = (measuredHeight / 2).toFloat()

        orb1.position.x = centerX
        orb1.position.y = centerY

        orb2.position.x = centerX
        orb2.position.y = centerY

        orb3.position.x = centerX
        orb3.position.y = centerY

        orb4.position.x = centerX
        orb4.position.y = centerY

        maxRadius = Math.min(measuredWidth, measuredHeight).toFloat() / 2
        updateSize()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (enableNoise) {
            noiseX.update()
            noiseY.update()
        }

        orb1.update()
        orb2.update()
        orb3.update()
        orb4.update()

        orb1.draw(canvas, noiseX.value, noiseY.value)
        orb2.draw(canvas, noiseX.value, noiseY.value)
        orb3.draw(canvas, noiseX.value, noiseY.value)
        orb4.draw(canvas, noiseX.value, noiseY.value)
    }

    class Pulse(val sizeRange: Vector, val position: Vector, opacity: Int, color: Int) {

        val paint: Paint = Paint()

        var size: RangedValue = RangedValue(sizeRange)

        init {
            paint.color = color
            paint.alpha = opacity
        }

        fun setColor(color: Int) {
            val opacity = paint.alpha
            paint.color = color
            paint.alpha = opacity
        }

        fun update() {
            size.update()
        }

        fun draw(canvas: Canvas?, noiseX: Float, noiseY: Float) {
            canvas?.drawCircle(position.x + noiseX, position.y + noiseY, size.value, paint)
        }
    }
}

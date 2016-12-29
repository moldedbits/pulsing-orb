package com.moldedbits.pulsingorb.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View

class FireFlies : View {
    val flies: MutableList<Fly> = mutableListOf()
    val connections: MutableList<Connection> = mutableListOf()

    val density: Float = 0.005f

    val distributionVariance: Float = 0.3f / density

    val pairingRadius: Float = 1f / density * 2

    val paint100: Paint = Paint()
    val paint75: Paint = Paint()
    val paint50: Paint = Paint()

    init {
        paint100.alpha = (255 * 0.5).toInt()
        paint75.alpha = (255 * 0.35).toInt()
        paint50.alpha = (255 * 0.2).toInt()
    }

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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (!isInEditMode) {
            populateFlies()
        }
    }

    private fun populateFlies() {
        flies.clear()

        // Populate Flies
        val step: Float = 1 / density

        val width: Float = measuredWidth.toFloat()
        val height: Float = measuredHeight.toFloat()

        var positionX: Float = Math.random().toFloat() * step
        var positionY: Float = Math.random().toFloat() * step
        var noise: Float

        while (positionY < height) {
            while (positionX < width) {

                noise = Math.random().toFloat()
                val paint = if (noise < 0.33) paint100 else if (noise < 0.66) paint75 else paint50
                noise = (noise * 2 - 1) * distributionVariance

                flies.add(Fly(Vector(positionX, positionY + noise), paint))

                positionX += (Math.random().toFloat() + 0.1f) * step
            }
            positionX = Math.random().toFloat() * step
            positionY += step
        }

        // Populate connections
        connections.clear()

        val connectionCount: Int = Math.sqrt(flies.size.toDouble()).toInt() * 4
        for (i in 0..connectionCount) {
            val index = getRandomFlyIndexWithoutConnection()
            val firstFly = flies[index]
            val otherFly: Fly = getNearestFly(firstFly)
            connections.add(Connection(firstFly, otherFly))
        }
    }

    private fun getRandomFlyIndexWithoutConnection(): Int {
        var index: Int = (Math.random() * flies.size).toInt()

        while (hasConnection(flies[index])) {
            index = (Math.random() * flies.size).toInt()
        }

        return index
    }

    private fun getNearestFly(fly: Fly): Fly {
        var minDistance: Float = -1f
        var nearestFly: Fly = fly
        var distance: Float
        for (otherFly in flies) {
            if (otherFly == fly) continue
            distance = fly.position.distanceTo(otherFly.position)
            if (minDistance < 0 || distance < minDistance) {
                minDistance = distance
                nearestFly = otherFly
            }
        }
        return nearestFly
    }

    private fun hasConnection(fly: Fly) : Boolean {
        return connections.any { it.first == fly || it.second == fly }
    }

    fun startPairing() {
        val center: Vector = Vector(width.toFloat() / 2, height.toFloat() / 2)
        val circle: Circle = Circle(center, pairingRadius)
        flies
                .filter { circle.contains(it.position) }
                .forEach {
                    it.startMovingToTarget(circle.nearestPointOnCircumference(it.position),
                            circle.center)
                }

        connections
                .filter { it.first.targetPosition.distanceTo(it.second.targetPosition) > pairingRadius }
                .forEach { connections.remove(it) }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        for (fly in flies) {
            fly.update()
            fly.draw(canvas)
        }

        for (connection in connections) {
            connection.draw(canvas)
        }
    }

    inner class Fly(val position: Vector, val paint: Paint) {
        val easing: Float = 0.15f
        val radius: Float = 20f
        val noiseFactor: Float = 2f

        val initialPosition: Vector = Vector(0f, 0f)
        val targetPosition: Vector = Vector(0f, 0f)
        val centerPoint: Vector = Vector(0f, 0f)

        var isMovingToTarget = false

        fun startMovingToTarget(targetPosition: Vector, centerPoint: Vector) {
            initialPosition.set(position)
            this.targetPosition.set(targetPosition)
            this.centerPoint.set(centerPoint)
            isMovingToTarget = true
        }

        fun update() {
            if (isMovingToTarget) {
                val dPos: Float = targetPosition.distanceTo(initialPosition)
                val theta: Double = Math.atan(((targetPosition.y - position.y) /
                        (targetPosition.x - position.x)).toDouble())
                position.x += dPos * Math.cos(theta).toFloat() * easing
                position.y += dPos * Math.sin(theta).toFloat() * easing

                if (centerPoint.distanceTo(position) >= centerPoint.distanceTo(targetPosition)) {
                    isMovingToTarget = false
                }
            } else {
                val noiseX: Float = (Math.random().toFloat() * 2 - 1) * noiseFactor
                val noiseY: Float = (Math.random().toFloat() * 2 - 1) * noiseFactor

                position.x += noiseX
                position.y += noiseY
            }
        }

        fun draw(canvas: Canvas?) {
            canvas?.drawCircle(position.x, position.y, radius, paint)
        }
    }

    inner class Connection(val first: Fly, val second: Fly) {

        fun draw(canvas: Canvas?): Unit {
            canvas?.drawLine(first.position.x, first.position.y, second.position.x,
                    second.position.y, paint75)
        }
    }


}
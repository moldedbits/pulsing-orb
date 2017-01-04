package com.moldedbits.pulsingorb.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View

class FireFlies : View {
    val flies: MutableList<Fly> = mutableListOf()
    val connections: MutableList<Connection> = mutableListOf()
    val pairedFlies: MutableList<PairedFly> = mutableListOf()
    val pairedConnections: MutableList<PairedConnection> = mutableListOf()

    val density: Float = 0.0035f

    val distributionVariance: Float = 0.3f / density

    var pairingRadius: Float = 0f
    var pairingOuterRadius: Float = 0f
    val outerPairingFactor = 1.4f

    val connectionThreshhold = 200f

    val paintPaired: Paint = Paint()
    val paintFlies: Paint = Paint()
    val paintConnection: Paint = Paint()
    val paintThick: Paint = Paint()

    var isPairing = false
    var isPaired = false

    var pairingCircle: Circle = Circle(Vector(0f, 0f), pairingRadius)

    init {
        paintPaired.color = Color.parseColor("#C7C7C7")
        paintFlies.color = Color.parseColor("#CFCFCF")
        paintThick.color = paintPaired.color
        paintThick.style = Paint.Style.FILL_AND_STROKE
        paintThick.strokeWidth = 8f
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
            pairingRadius = measuredWidth * 0.3f
            pairingOuterRadius = pairingRadius * outerPairingFactor
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
                noise = (noise * 2 - 1) * distributionVariance

                flies.add(Fly(Vector(positionX, positionY + noise), paintFlies))

                positionX += (Math.random().toFloat() + 0.1f) * step
            }
            positionX = Math.random().toFloat() * step
            positionY += step
        }

        updateConnections()
    }

    fun updateConnections() {
        connections.clear()

        for (fly in flies) {
            val otherFly: Fly = getNearestFly(fly)
            if (otherFly.position.distanceTo(fly.position) < connectionThreshhold) {
                connections.add(Connection(fly, otherFly))
            }
        }
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

    fun showPairing() {
        if (isPaired || isPairing) {
            return
        }

        isPairing = true

        val center: Vector = Vector(width.toFloat() / 2, height.toFloat() / 2)
        pairingCircle.radius = pairingRadius
        pairingCircle.center.set(center)

        val outerPairingCircle = Circle(center, pairingOuterRadius)

        flies
                .filter { outerPairingCircle.contains(it.position) }
                .forEach {
                    it.startMovingToTarget(pairingCircle.nearestPointOnCircumference(it.position))
                }

        connections
                .filter { it.first.targetPosition.distanceTo(it.second.targetPosition) > pairingRadius }
                .forEach { connections.remove(it) }

        postDelayed(showFirstPairedFly, 200)
    }

    private val showFirstPairedFly: Runnable = Runnable {
        val center: Vector = Vector(width.toFloat() / 2, height.toFloat() / 2)

        val position = Vector(center.x - (Math.random() * 0.7f + 0.3f).toFloat() * pairingRadius * 0.6f,
                center.y + (Math.random().toFloat() * 2 - 1) * pairingRadius * 0.1f)

        pairedFlies.add(PairedFly(position, paintPaired, RangedValue(Vector(40f, 50f)),
                zoomCompleteListener = object: PairedFlyZoomCompleteListener {
                    override fun onZoomComplete() {
                        showSecondPairedFly()
                    }
                }))
    }

    fun showSecondPairedFly() {
        val center: Vector = Vector(width.toFloat() / 2, height.toFloat() / 2)

        val position = Vector(center.x + (Math.random() * 0.7f + 0.3f).toFloat() * pairingRadius * 0.6f,
                center.y + (Math.random().toFloat() * 2 - 1) * pairingRadius * 0.1f)

        pairedFlies.add(PairedFly(position, paintPaired, RangedValue(Vector(40f, 50f)),
                zoomCompleteListener = object: PairedFlyZoomCompleteListener {
                    override fun onZoomComplete() {
                        showPaired()
                    }
                }))
    }

    fun showPaired() {
        if (!isPairing || isPaired) {
            return
        }

        isPaired = true

        pairedConnections.add(PairedConnection(pairedFlies[0], pairedFlies[1], paintThick))
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        // First update all flies
        for (fly in flies) {
            fly.update()
        }

        updateConnections()

        // Draw connections before flies as connections are translucent
        for (connection in connections) {
            connection.draw(canvas)
        }

        for (fly in flies) {
            fly.draw(canvas)
        }

        for (pairedFly in pairedFlies) {
            pairedFly.update()
            pairedFly.draw(canvas)
        }

        for (pairedConnection in pairedConnections) {
            pairedConnection.update()
            pairedConnection.draw(canvas)
        }
    }

    interface PairedFlyZoomCompleteListener {
        fun onZoomComplete()
    }

    inner class PairedFly(position: Vector, paint: Paint, radius: RangedValue,
                          val zoomEasing: Float = 0.03f,
                          val zoomCompleteListener: PairedFlyZoomCompleteListener? = null) :
            Fly(position, paint, radius) {

        var isZoomingIn: Boolean = true
        var zoomRadius: Float = 0f

        init {
            noiseFactor = 0.5f
        }

        override fun update() {
            if (isZoomingIn) {
                val dPos: Float = radius.range.y - 0f
                zoomRadius += dPos * zoomEasing

                if (zoomRadius > radius.range.y) {
                    isZoomingIn = false
                    radius.value = radius.range.y

                    zoomCompleteListener?.onZoomComplete()
                }
            } else {
                super.update()
            }
        }

        override fun draw(canvas: Canvas?) {
            if (isZoomingIn) {
                canvas?.drawCircle(position.x, position.y, zoomRadius, paint)
            } else {
                super.draw(canvas)
            }
        }
    }

    open inner class Fly(position: Vector, paint: Paint,
                         radius: RangedValue = RangedValue(Vector(12f, 16f))) :
            PulsingCircle(position, paint, radius) {

        val easing: Float = 0.02f
        open var noiseFactor: Float = 2.5f

        val initialPosition: Vector = Vector(0f, 0f)
        val targetPosition: Vector = Vector(0f, 0f)

        var isMovingToTarget = false
        var isStuckToTarget = false

        var isMovingOutward = false

        var stuckCounter: Int = 0
        val STICK_DURATION: Int = 500

        fun startMovingToTarget(targetPosition: Vector, stickAtTarget: Boolean = false) {
            initialPosition.set(position)
            this.targetPosition.set(targetPosition)
            isMovingToTarget = true
            isStuckToTarget = stickAtTarget

            isMovingOutward = pairingCircle.center.distanceTo(targetPosition) >
                    pairingCircle.center.distanceTo(initialPosition)
        }

        override fun update() {
            super.update()
            if (isMovingToTarget) {
                val dPos: Float = targetPosition.distanceTo(initialPosition)
                val theta: Double = Math.atan(((targetPosition.y - position.y) /
                        (targetPosition.x - position.x)).toDouble())

                val direction = if (targetPosition.x > initialPosition.x) 1 else -1

                position.x += dPos * Math.cos(theta).toFloat() * easing * direction
                position.y += dPos * Math.sin(theta).toFloat() * easing * direction

                val positionFurther = pairingCircle.center.distanceTo(position) >=
                        pairingCircle.center.distanceTo(targetPosition)
                if ((isMovingOutward && positionFurther) || (!isMovingOutward && !positionFurther)) {
                    isMovingToTarget = false
                }
            } else {
                val noiseX: Float = (Math.random().toFloat() * 2 - 1) * noiseFactor
                val noiseY: Float = (Math.random().toFloat() * 2 - 1) * noiseFactor

                position.x += noiseX
                position.y += noiseY

                if (isStuckToTarget) {
                    if (stuckCounter > STICK_DURATION) {
                        isStuckToTarget = false
                        return
                    }

                    stuckCounter++

                    position.set(pairingCircle.nearestPointOnCircumference(position))
                }
            }
        }
    }

    open inner class PulsingCircle(val position: Vector, val paint: Paint,
                                   val radius: RangedValue = RangedValue(Vector(15f, 25f))) {

        open fun update() {
            radius.update()
        }

        open fun draw(canvas: Canvas?) {
            canvas?.drawCircle(position.x, position.y, radius.value, paint)
        }
    }

    inner class Connection(val first: Fly, val second: Fly, val paint: Paint = paintConnection) {

        fun draw(canvas: Canvas?): Unit {

            paint.alpha = ((1 - first.position.distanceTo(second.position) / connectionThreshhold)
                    * 255 * 0.4f).toInt()
            canvas?.drawLine(first.position.x, first.position.y, second.position.x,
                    second.position.y, paint)
        }
    }

    inner class PairedConnection(val first: Fly, val second: Fly, val paint: Paint = paintPaired) {

        var percentageComplete: Float = 0f

        fun update() {
            if (percentageComplete < 1f) {
                percentageComplete += 0.02f
            }
        }

        fun draw(canvas: Canvas?) {
            val endX = (second.position.x - first.position.x) * percentageComplete + first.position.x
            val endY = (second.position.y - first.position.y) * percentageComplete + first.position.y

            canvas?.drawLine(first.position.x, first.position.y, endX, endY, paint)
        }
    }
}
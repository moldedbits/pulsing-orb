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

    val density: Float = 0.005f

    val distributionVariance: Float = 0.3f / density

    val pairingRadius: Float = 1f / density * 2

    val paint100: Paint = Paint()
    val paint75: Paint = Paint()
    val paint50: Paint = Paint()
    val paint150: Paint = Paint()

    val paintThick: Paint = Paint()

    var isPairing = false
    var isPaired = false

    var pairingCircle: Circle = Circle(Vector(0f, 0f), pairingRadius)

    init {
        paint150.color = Color.parseColor("#999999")
        paint100.color = Color.parseColor("#AAAAAA")
        paint75.color = Color.parseColor("#BBBBBB")
        paint50.color = Color.parseColor("#CCCCCC")
        paintThick.color = paint150.color
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

    fun showPairing() {
        if (isPaired || isPairing) {
            return
        }

        isPairing = true

        val center: Vector = Vector(width.toFloat() / 2, height.toFloat() / 2)
        pairingCircle.center.set(center)

        flies
                .filter { pairingCircle.contains(it.position) }
                .forEach {
                    it.startMovingToTarget(pairingCircle.nearestPointOnCircumference(it.position))
                }

        connections
                .filter { it.first.targetPosition.distanceTo(it.second.targetPosition) > pairingRadius }
                .forEach { connections.remove(it) }

        post(showFirstPairedFly)
    }

    private val showFirstPairedFly: Runnable = Runnable {
        val center: Vector = Vector(width.toFloat() / 2, height.toFloat() / 2)

        val position = Vector(center.x - (Math.random() * 0.7f + 0.3f).toFloat() * pairingRadius * 0.6f,
                center.y + (Math.random().toFloat() * 2 - 1) * pairingRadius * 0.1f)

        pairedFlies.add(PairedFly(position, paint150, RangedValue(Vector(40f, 50f)),
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

        pairedFlies.add(PairedFly(position, paint150, RangedValue(Vector(40f, 50f)),
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

        for (fly in flies) {
            fly.update()
            fly.draw(canvas)
        }

        for (connection in connections) {
            connection.draw(canvas)
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
                         radius: RangedValue = RangedValue(Vector(10f, 14f))) :
            PulsingCircle(position, paint, radius) {

        val easing: Float = 0.05f
        open var noiseFactor: Float = 1f

        val initialPosition: Vector = Vector(0f, 0f)
        val targetPosition: Vector = Vector(0f, 0f)

        var isMovingToTarget = false
        var isStuckToTarget = false

        var stuckCounter: Int = 0
        val STICK_DURATION: Int = 500

        fun startMovingToTarget(targetPosition: Vector, stickAtTarget: Boolean = false) {
            initialPosition.set(position)
            this.targetPosition.set(targetPosition)
            isMovingToTarget = true
            isStuckToTarget = stickAtTarget
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

                if (pairingCircle.center.distanceTo(position) >=
                        pairingCircle.center.distanceTo(targetPosition)) {
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

    inner class Connection(val first: Fly, val second: Fly, val paint: Paint = paint75) {

        fun draw(canvas: Canvas?): Unit {
            canvas?.drawLine(first.position.x, first.position.y, second.position.x,
                    second.position.y, paint)
        }
    }

    inner class PairedConnection(val first: Fly, val second: Fly, val paint: Paint = paint75) {

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
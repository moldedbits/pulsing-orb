package com.moldedbits.pulsingorb.widget

import android.util.Log

class Circle(val center: Vector, val radius: Float) {

    fun contains(point: Vector) : Boolean {
        return center.distanceTo(point) < radius
    }

    fun nearestPointOnCircumference(point: Vector) : Vector {
        val bx = point.x
        val by = point.y
        val ax = center.x
        val ay = center.y

        val distanceFromCenter: Float = Math.sqrt(
                ((bx - ax)*(bx - ax)).toDouble() + ((by - ay)*(by - ay)).toDouble()
        ).toFloat()
        val finalX: Float = ax+radius*((bx - ax) / distanceFromCenter)
        val finalY: Float = ay+radius*((by - ay) / distanceFromCenter)

        Log.d("Circle", "Initial Point: ${point.x - center.x} ${point.y - center.y}")
        Log.d("Circle", "Target Point: ${finalX - center.x} ${finalY - center.y}")

        return Vector(finalX, finalY)
    }
}
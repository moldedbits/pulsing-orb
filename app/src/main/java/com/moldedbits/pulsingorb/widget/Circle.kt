package com.moldedbits.pulsingorb.widget

class Circle(val center: Vector, val radius: Float) {

    fun contains(point: Vector) : Boolean {
        return center.distanceTo(point) < radius
    }

    fun nearestPointOnCircumference(point: Vector) : Vector {
        val theta: Double = Math.atan(((point.x - center.x) / (point.y - center.y)).toDouble())
        return Vector(radius * Math.cos(theta).toFloat(), radius * Math.sin(theta).toFloat())
    }
}
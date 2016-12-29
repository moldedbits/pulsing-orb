package com.moldedbits.pulsingorb.widget

class Vector(var x: Float, var y: Float) {

    fun distanceTo(other: Vector): Float {
        return Math.sqrt(((other.x - x) * (other.x - x) +
                (other.y - y) * (other.y - y)).toDouble()).toFloat()
    }

    fun set(target: Vector) {
        x = target.x
        y = target.y
    }

    override fun toString(): String {
        return "::$x $y::"
    }
}
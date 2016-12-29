package com.moldedbits.pulsingorb.widget

class RangedValue(val range: Vector, val easing: Float = 0.01f, val randomize: Boolean = false) {

    var value: Float = range.x

    var upCycle: Boolean = true

    var counter: Int = 0

    fun update() {
        var dPos: Float = range.y - range.x
        counter = (++counter % 5)
        if (randomize) {
            dPos *= Math.random().toFloat()
            if (counter == 0) {
                if (Math.random() < 0.5) {
                    upCycle = !upCycle
                }
            }
        }
        if (upCycle) {
            value += dPos * easing
            upCycle = value < range.y
        } else {
            value -= dPos * easing
            upCycle = value < range.x
        }
    }
}
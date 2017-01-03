package com.moldedbits.pulsingorb

import com.moldedbits.pulsingorb.widget.Circle
import com.moldedbits.pulsingorb.widget.Vector
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CircleTest {

    @Test
    fun nearestPoint() {
        val circle: Circle = Circle(Vector(1f, 1f), 1f)

        val firstQuadrant = circle.nearestPointOnCircumference(Vector(1.25f, 1.25f))
        assert(firstQuadrant.x == Math.cos(Math.PI / 4).toFloat() + 1, { "First quadrant" })
        assert(firstQuadrant.y == Math.sin(Math.PI / 4).toFloat() + 1, { "First quadrant" })

        val secondQuadrant = circle.nearestPointOnCircumference(Vector(0.75f, 1.25f))
        assert(secondQuadrant.x == Math.cos(Math.PI * 3 / 4).toFloat() + 1, { "Second quadrant" })
        assert(secondQuadrant.y == Math.sin(Math.PI * 3 / 4).toFloat() + 1, { "Second quadrant" })

        val thirdQuadrant = circle.nearestPointOnCircumference(Vector(0.75f, 0.75f))
        assert(thirdQuadrant.x == Math.cos(Math.PI * 5 / 4).toFloat() + 1, { "Third quadrant" })
        assert(thirdQuadrant.y == Math.sin(Math.PI * 5 / 4).toFloat() + 1, { "Third quadrant" })

        val fourthQuadrant = circle.nearestPointOnCircumference(Vector(1.25f, 0.75f))
        assert(fourthQuadrant.x == Math.cos(Math.PI * 7 / 4).toFloat() + 1, { "Fourth quadrant" })
        assert(fourthQuadrant.y == Math.sin(Math.PI * 7 / 4).toFloat() + 1, { "Fourth quadrant" })
    }
}
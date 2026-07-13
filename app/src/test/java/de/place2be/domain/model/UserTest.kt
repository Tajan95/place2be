package de.place2be.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class UserTest {
    @Test
    fun userScore_defaultsToZero() {
        assertEquals(0, User(displayName = "Alex").userScore)
    }

    @Test
    fun userScore_mustNotBeNegative() {
        assertThrows(IllegalArgumentException::class.java) {
            User(displayName = "Alex", userScore = -1)
        }
    }
}

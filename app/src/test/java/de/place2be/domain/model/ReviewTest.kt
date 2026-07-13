package de.place2be.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.UUID

class ReviewTest {
    @Test
    fun score_isAverageOfVibeSafetyAndAccessibility() {
        val review = review(vibe = 5, safety = 4, accessibility = 3)

        assertEquals(4.0, review.score, 0.0)
    }

    @Test
    fun text_isOptionalButMustNotBeBlank() {
        assertEquals(null, review().text)
        assertThrows(IllegalArgumentException::class.java) {
            review(text = " ")
        }
    }

    @Test
    fun reactions_mustNotBeNegative() {
        assertThrows(IllegalArgumentException::class.java) {
            review(likes = -1)
        }
    }

    private fun review(
        vibe: Int = 3,
        safety: Int = 3,
        accessibility: Int = 3,
        text: String? = null,
        likes: Int = 0,
    ) = Review(
        placeUuid = UUID.fromString("11111111-1111-4111-8111-111111111111"),
        userUuid = UUID.fromString("aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaaa"),
        vibe = vibe,
        safety = safety,
        accessibility = accessibility,
        timestampMillis = 1_700_000_000_000,
        text = text,
        likes = likes,
    )
}

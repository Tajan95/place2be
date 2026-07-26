package de.place2be.feature.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeaderBannerContentTest {
    @Test
    fun `banner contains only clearly labelled mock advertisements`() {
        val items = buildHeaderBannerItems()

        assertEquals(4, items.size)
        assertTrue(items.all { it.label == "Anzeige" || it.label == "Werbeplatz" })
        assertFalse(
            items.any { item ->
                item.message.contains("Filter", ignoreCase = true) ||
                    item.message.contains("gespeichert", ignoreCase = true) ||
                    item.message.contains("Feature", ignoreCase = true)
            },
        )
    }

    @Test
    fun `banner alternates visually distinct mock advertising palettes`() {
        val palettes = buildHeaderBannerItems().map(HeaderBannerItem::palette)

        assertEquals(
            listOf(
                HeaderBannerPalette.CYAN,
                HeaderBannerPalette.PINK,
                HeaderBannerPalette.CYAN,
                HeaderBannerPalette.PINK,
            ),
            palettes,
        )
    }

    @Test
    fun `advertising placeholder is part of rotation`() {
        assertTrue(
            buildHeaderBannerItems().any { item ->
                item.message == "Hier könnte Ihre Werbung stehen!"
            },
        )
    }

    @Test
    fun `advertisements rotate every fifteen seconds`() {
        assertEquals(15_000L, HEADER_BANNER_ROTATION_MILLIS)
    }

    @Test
    fun `disabled rotation keeps current advertisement static`() {
        assertEquals(
            2,
            nextHeaderBannerIndex(
                currentIndex = 2,
                itemCount = 4,
                rotationEnabled = false,
            ),
        )
    }

    @Test
    fun `rotation wraps after last advertisement`() {
        assertEquals(
            0,
            nextHeaderBannerIndex(
                currentIndex = 3,
                itemCount = 4,
                rotationEnabled = true,
            ),
        )
    }
}

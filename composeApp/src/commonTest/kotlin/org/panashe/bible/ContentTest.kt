package org.panashe.bible

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContentTest {
    @Test
    fun routesMatchPublicSite() {
        assertEquals("/", PanasheRoute.Daily.path)
        assertEquals("/bible", PanasheRoute.Bible.path)
        assertEquals("/bible/church", PanasheRoute.Communion.path)
        assertEquals("/about", PanasheRoute.About.path)
        assertEquals("/privacy", PanasheRoute.Privacy.path)
    }

    @Test
    fun dailyReadingHasScripture() {
        assertTrue(todayReading.verses.isNotEmpty())
    }
}


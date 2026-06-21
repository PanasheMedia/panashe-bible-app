package org.panashe.bible

import org.panashe.bible.shared.SharedConstants
import kotlin.test.Test
import kotlin.test.assertEquals

class ContentTest {
    @Test
    fun routesMatchSharedContract() {
        assertEquals(SharedConstants.ROUTE_DAILY, PanasheRoute.Daily.path)
        assertEquals(SharedConstants.ROUTE_BIBLE, PanasheRoute.Bible.path)
        assertEquals(SharedConstants.ROUTE_COMMUNION, PanasheRoute.Communion.path)
        assertEquals(SharedConstants.ROUTE_ABOUT, PanasheRoute.About.path)
        assertEquals(SharedConstants.ROUTE_PRIVACY, PanasheRoute.Privacy.path)
    }

    @Test
    fun formatsIsoDate() {
        assertEquals("June 1, 2026", formatDate("2026-06-01"))
        assertEquals("December 25, 2026", formatDate("2026-12-25"))
    }
}

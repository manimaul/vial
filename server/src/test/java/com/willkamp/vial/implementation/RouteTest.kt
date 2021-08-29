package com.willkamp.vial.implementation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RouteTest {

    @Test
    @Throws(Exception::class)
    fun testWildcard() {
        val matches1 = Route.build(".*")
        assertTrue(matches1.matches("/api/people/jeff"))
    }

    @Test
    @Throws(Exception::class)
    fun testNamedWildcard() {
        val route = Route.build("/api/people/:*path")
        val path = "/api/people/jimbo/pockets/chaw"
        assertTrue(route.matches(path))
    }

    @Test
    @Throws(Exception::class)
    fun testPathPattern() {
        val pathPattern1 = Route.build("/api/people/:person").pathPattern().toString()
        assertEquals("/api/people/(?<person>[^/]*)[/]?", pathPattern1)
        val pathPattern2 = Route.build("/api/people/:person/hands/:hand/slap").pathPattern().toString()
        assertEquals("/api/people/(?<person>[^/]*)/hands/(?<hand>[^/]*)/slap[/]?", pathPattern2)
    }

    @Test
    @Throws(Exception::class)
    fun testMatches() {
        val matches1 = Route.build("/api/people/:person")
        assertTrue(matches1.matches("/api/people/jeff"))
    }

    @Test
    @Throws(Exception::class)
    fun testMatchesFileExtension() {
        val route = Route.build("/api/font/:filename")
        val path = "/api/font/sans.pbf"
        assertTrue(route.matches(path))
        val groups = route.groups(path)
        assertEquals(setOf("filename"), groups.keys)
        assertEquals("sans.pbf", groups["filename"])
    }

    @Test
    @Throws(Exception::class)
    fun testMatchesAny() {
        val matches1 = Route.build(".*")
        assertTrue(matches1.matches("/api/people/jeff"))
    }

    @Test
    @Throws(Exception::class)
    fun testGroups() {
        val group1 = Route.build("/api/people/:person").groups("/api/people/jeff")
        assertEquals(group1["person"], "jeff")
        val group2 = Route.build("/api/people/:person/hands/:hand/slap")
                .groups("/api/people/jeff/hands/left/slap")
        assertEquals(group2["person"], "jeff")
        assertEquals(group2["hand"], "left")
    }
}

package com.github.jclsn.codesonar.model

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StatusTest {
    private val loaded = Status.parse(
        JsonParser.parseString(
            """{"loaded": true, "hidden": false, "hub": "http://hub:7340", "project": "test_project",
                "presets": ["security", "cert"], "conf": "/ws/codesonar.conf", "name": "test_project analysis 3",
                "key": "abc", "hubId": "12", "url": "http://hub:7340/analysis/12.html", "total": 9, "shown": 7,
                "counts": {"unchanged": 5, "moved": 2, "changed": 2, "unverified": 0}}""",
        ),
    )

    @Test
    fun `parses the status of a loaded analysis`() {
        assertTrue(loaded.loaded)
        assertEquals("http://hub:7340", loaded.hub)
        assertEquals("test_project", loaded.project)
        assertEquals(listOf("security", "cert"), loaded.presets)
        assertEquals("/ws/codesonar.conf", loaded.conf)
        assertEquals("12", loaded.hubId)
        assertEquals("http://hub:7340/analysis/12.html", loaded.url)
        assertEquals(9, loaded.total)
        assertEquals(7, loaded.shown)
        assertEquals(listOf("unchanged" to 5, "moved" to 2, "changed" to 2), loaded.details())
    }

    @Test
    fun `parses the status without an analysis`() {
        val status = Status.parse(
            JsonParser.parseString(
                """{"loaded": false, "hidden": false, "hub": null, "project": null, "presets": [], "conf": null}""",
            ),
        )

        assertFalse(status.loaded)
        assertNull(status.hub)
        assertNull(status.conf)
        assertNull(status.url)
        assertEquals(emptyList(), status.details())
        assertEquals(Status(counts = Status.KINDS.associateWith { 0 }), status)
    }

    @Test
    fun `takes a numeric hub id`() {
        assertEquals("12", Status.parse(JsonParser.parseString("""{"loaded": true, "hubId": 12}""")).hubId)
    }

    @Test
    fun `survives anything else`() {
        assertEquals(Status(), Status.parse(null))
        assertEquals(Status(), Status.parse(JsonParser.parseString("[]")))
    }

    @Test
    fun `summarizes the analysis`() {
        assertEquals(
            "test_project analysis 3: 7 of 9 warnings shown (5 unchanged, 2 moved, 2 changed)",
            loaded.summary(),
        )
        assertEquals(
            "demo: 3 of 3 warnings shown",
            Status(loaded = true, name = "demo", total = 3, shown = 3).summary(),
        )
        assertEquals("No analysis loaded", Status().summary())
        assertEquals("Hiding the analysis...", loaded.copy(hidden = true).summary())
    }
}

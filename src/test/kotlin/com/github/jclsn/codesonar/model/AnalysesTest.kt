package com.github.jclsn.codesonar.model

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnalysesTest {
    private val result = Analyses.parse(
        JsonParser.parseString(
            """{"project": "test_project", "analyses": [
                {"id": 14, "name": "nightly", "started": "2026-11-20 03:00", "state": "ANALYZING",
                 "finished": false, "current": false, "cached": false},
                {"id": 12, "name": "release", "started": "2026-11-19 10:15", "state": "FINISHED",
                 "finished": true, "current": true, "cached": true},
                {"id": 7, "name": "first", "started": "2026-11-01 09:00", "state": "FINISHED",
                 "finished": true, "current": false, "cached": false}]}""",
        ),
    )

    @Test
    fun `parses the analyses of the project`() {
        assertEquals("test_project", result.project)
        assertEquals(listOf(14L, 12L, 7L), result.analyses.map { it.id })
        assertEquals("release", result.analyses[1].name)
        assertTrue(result.analyses[1].current)
        assertTrue(result.analyses[1].cached)
        assertFalse(result.analyses[0].finished)
        assertFalse(result.deleted)
        assertNull(result.message)
    }

    @Test
    fun `parses a refused deletion`() {
        val refused = Analyses.parse(
            JsonParser.parseString(
                """{"project": "p", "analyses": [], "deleted": false, "message": "permission denied"}""",
            ),
        )

        assertFalse(refused.deleted)
        assertEquals("permission denied", refused.message)
        assertTrue(Analyses.parse(JsonParser.parseString("""{"project": "p", "analyses": [], "deleted": true}""")).deleted)
    }

    @Test
    fun `marks the shown, loading, unfinished and cached analyses`() {
        val (running, shown, old) = result.analyses

        assertEquals("    14  nightly  2026-11-20 03:00  [ANALYZING]", running.line())
        assertEquals("*   12  release  2026-11-19 10:15  [cached]", shown.line())
        assertEquals("     7  first  2026-11-01 09:00", old.line())
        assertEquals("~    7  first  2026-11-01 09:00", old.copy(loading = true).line())
    }
}

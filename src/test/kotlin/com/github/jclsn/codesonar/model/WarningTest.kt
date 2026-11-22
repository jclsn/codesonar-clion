package com.github.jclsn.codesonar.model

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WarningTest {
    private val warnings = Warning.parseList(
        JsonParser.parseString(
            """[{"uri": "file:///ws/src/main.c",
                 "range": {"start": {"line": 15, "character": 4}, "end": {"line": 16, "character": 9}},
                 "severity": 1, "code": "LANG.MEM.NPD", "message": "Null Pointer Dereference",
                 "status": "moved", "hubUri": "http://hub:7340/warninginstance/42.html"},
                {"uri": "file:///ws/my%20lib/util.c",
                 "range": {"start": {"line": 2, "character": 0}, "end": {"line": 2, "character": 0}},
                 "severity": 2, "message": "Unused Value", "status": "unchanged", "hubUri": null}]""",
        ),
    )

    @Test
    fun `parses the shown warnings`() {
        val (first, second) = warnings

        assertEquals("/ws/src/main.c", first.path)
        assertEquals(15, first.line)
        assertEquals(4, first.column)
        assertEquals(16, first.endLine)
        assertEquals(1, first.severity)
        assertEquals("moved", first.status)
        assertEquals("http://hub:7340/warninginstance/42.html", first.hubUri)
        assertEquals("[LANG.MEM.NPD] Null Pointer Dereference", first.text())
        assertEquals("Unused Value", second.text())
        assertNull(second.hubUri)
    }

    @Test
    fun `decodes the paths of uris`() {
        assertEquals("/ws/my lib/util.c", warnings[1].path)
        assertEquals("util.c", uriFileName(warnings[1].uri))
        assertNull(uriToPath("http://hub/file.c"))
        assertNull(uriToPath("not a uri"))
        assertEquals("file.c", uriFileName("http://hub/file.c"))
    }

    @Test
    fun `covers its lines of its file`() {
        val warning = warnings[0]

        assertTrue(warning.covers("/ws/src/main.c", 15))
        assertTrue(warning.covers("/ws/src/main.c", 16))
        assertFalse(warning.covers("/ws/src/main.c", 14))
        assertFalse(warning.covers("/ws/src/main.c", 17))
        assertFalse(warning.covers("/ws/src/other.c", 15))
    }
}

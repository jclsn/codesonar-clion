package com.github.jclsn.codesonar.model

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolutionTest {
    @Test
    fun `picks the warnings of a line`() {
        val argument = ResolveArgument.line("file:///ws/main.c", 4, Resolution.FALSE_POSITIVE)

        assertEquals(
            JsonParser.parseString(
                """{"uri": "file:///ws/main.c", "resolution": "falsePositive",
                    "range": {"start": {"line": 4, "character": 0}, "end": {"line": 5, "character": 0}}}""",
            ),
            argument,
        )
    }

    @Test
    fun `asks the server to leave the outcome to the client`() {
        val action = JsonParser.parseString("""{"uri": "file:///ws/main.c", "fingerprint": "abc", "resolution": "fixed"}""")
        val plain = mapOf("uri" to "file:///ws/main.c", "fingerprint" to "abc", "resolution" to "fixed")
        val quiet = JsonParser.parseString(
            """{"uri": "file:///ws/main.c", "fingerprint": "abc", "resolution": "fixed", "quiet": true}""",
        )

        assertEquals(quiet, ResolveArgument.quiet(action))
        assertEquals(quiet, ResolveArgument.quiet(plain))
        // The argument of the code action is left alone.
        assertEquals(false, action.asJsonObject.has("quiet"))
    }

    @Test
    fun `parses the outcome`() {
        val result = ResolveResult.parse(
            JsonParser.parseString("""{"resolved": 1, "message": "Marked LANG.MEM.NPD as fixed on the hub"}"""),
        )

        assertEquals(ResolveResult(1, "Marked LANG.MEM.NPD as fixed on the hub"), result)
        assertEquals(ResolveResult(), ResolveResult.parse(null))
    }
}

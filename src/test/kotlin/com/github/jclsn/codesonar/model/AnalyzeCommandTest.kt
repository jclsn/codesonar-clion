package com.github.jclsn.codesonar.model

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AnalyzeCommandTest {
    private fun json(text: String) = JsonParser.parseString(text)

    @Test
    fun `parses the command of an analysis`() {
        val command = AnalyzeCommand.parse(
            json(
                """{"command": ["codesonar", "analyze", ".codesonar/project", "-foreground", "make"],
                    "cwd": "/ws", "temporaryFiles": ["/tmp/codesonar-password"]}""",
            ),
        )

        assertEquals(listOf("codesonar", "analyze", ".codesonar/project", "-foreground", "make"), command?.command)
        assertEquals("/ws", command?.cwd)
        assertEquals(listOf("/tmp/codesonar-password"), command?.temporaryFiles)
        assertNull(AnalyzeCommand.parse(json("""{"command": [], "cwd": "/ws"}""")))
        assertNull(AnalyzeCommand.parse(null))
    }
}

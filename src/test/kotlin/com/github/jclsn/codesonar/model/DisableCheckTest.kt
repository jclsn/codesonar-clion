package com.github.jclsn.codesonar.model

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals

class DisableCheckTest {
    private fun warning(line: Int, code: String) =
        Warning("file:///ws/main.c", line, 0, line, 5, 2, code, "message", "unchanged", null)

    @Test
    fun `names the check`() {
        assertEquals(
            JsonParser.parseString("""{"class": "Null Pointer Dereference"}"""),
            DisableCheckArgument.of("Null Pointer Dereference"),
        )
    }

    @Test
    fun `finds the checks of the warnings in a line`() {
        val warnings = listOf(
            warning(3, "Unused Value"),
            warning(15, "Division By Zero"),
            warning(15, "Unused Value"),
            warning(15, "Division By Zero"),
            warning(15, ""),
        )

        assertEquals(listOf("Division By Zero", "Unused Value"), DisableCheckArgument.classes(warnings, "/ws/main.c", 15))
        assertEquals(listOf("Unused Value"), DisableCheckArgument.classes(warnings, "/ws/main.c", 3))
        assertEquals(emptyList(), DisableCheckArgument.classes(warnings, "/ws/main.c", 4))
        assertEquals(emptyList(), DisableCheckArgument.classes(warnings, "/ws/other.c", 15))
        assertEquals(emptyList(), DisableCheckArgument.classes(warnings, null, null))
    }

    @Test
    fun `parses the outcome`() {
        val result = DisableCheckResult.parse(
            JsonParser.parseString(
                """{"disabled": true, "message": "Disabled the check for 'Unused Value' in codesonar.conf"}""",
            ),
        )

        assertEquals(DisableCheckResult(true, "Disabled the check for 'Unused Value' in codesonar.conf"), result)
        assertEquals(DisableCheckResult(), DisableCheckResult.parse(null))
    }
}

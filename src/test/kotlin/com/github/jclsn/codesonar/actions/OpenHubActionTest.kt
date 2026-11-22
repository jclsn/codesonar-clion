package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.model.Warning
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OpenHubActionTest {
    private fun warning(line: Int, hubUri: String?) =
        Warning("file:///ws/main.c", line, 0, line, 5, 2, "LANG.MEM.NPD", "Null Pointer Dereference", "unchanged", hubUri)

    @Test
    fun `opens the warning at the caret on the hub`() {
        val warnings = listOf(warning(3, null), warning(15, null), warning(15, "http://hub/w/42"), warning(20, "http://hub/w/43"))

        assertEquals("http://hub/w/42", OpenHubAction.hubUrl(warnings, "/ws/main.c", 15))
        assertEquals("http://hub/w/43", OpenHubAction.hubUrl(warnings, "/ws/main.c", 20))
        assertNull(OpenHubAction.hubUrl(warnings, "/ws/main.c", 3))
        assertNull(OpenHubAction.hubUrl(warnings, "/ws/other.c", 15))
        assertNull(OpenHubAction.hubUrl(warnings, null, null))
    }
}

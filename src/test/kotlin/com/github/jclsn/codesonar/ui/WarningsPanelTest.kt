package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.model.Status
import kotlin.test.Test
import kotlin.test.assertEquals

class WarningsPanelTest {
    @Test
    fun `names the list of warnings after the analysis`() {
        assertEquals("nightly", WarningsPanel.title(Status(loaded = true, name = "nightly")))
        assertEquals("", WarningsPanel.title(Status()))
    }
}

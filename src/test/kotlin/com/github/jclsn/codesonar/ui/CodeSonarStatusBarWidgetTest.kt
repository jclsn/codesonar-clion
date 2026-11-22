package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.model.Status
import kotlin.test.Test
import kotlin.test.assertEquals

class CodeSonarStatusBarWidgetTest {
    @Test
    fun `shows the analysis in the status bar`() {
        val loaded = Status(loaded = true, name = "nightly", total = 9, shown = 7)

        assertEquals("CodeSonar: 7/9", CodeSonarStatusBarWidget.text(loaded, loading = false))
        assertEquals("CodeSonar: loading...", CodeSonarStatusBarWidget.text(loaded, loading = true))
        assertEquals("CodeSonar: hidden", CodeSonarStatusBarWidget.text(loaded.copy(hidden = true), loading = false))
        assertEquals("CodeSonar", CodeSonarStatusBarWidget.text(Status(), loading = false))
    }
}

package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.model.Analysis
import kotlin.test.Test
import kotlin.test.assertEquals

class AnalysesPopupTest {
    private fun analysis(id: Long, current: Boolean = false, loading: Boolean = false) =
        Analysis(id, "a$id", "2026-11-20", "FINISHED", finished = true, current = current, cached = false, loading = loading)

    @Test
    fun `marks the shown analysis in the list`() {
        val analyses = listOf(analysis(14, loading = true), analysis(12, current = true), analysis(7))

        AnalysesPopup.mark(analyses, "14")
        assertEquals(listOf(true, false, false), analyses.map { it.current })
        assertEquals(listOf(false, false, false), analyses.map { it.loading })

        // Nothing is shown after hiding the analysis.
        AnalysesPopup.mark(analyses, null)
        assertEquals(listOf(false, false, false), analyses.map { it.current })
    }
}

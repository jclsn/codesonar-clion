package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.model.Status
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class StatusHtmlTest {
    private val palette = Palette("#accent", "#changed", "#muted", "#unchanged", "#moved")
    private val loaded = Status(
        loaded = true,
        hub = "http://hub:7340",
        project = "test_project",
        presets = listOf("security", "cert"),
        conf = "/ws/codesonar.conf",
        name = "nightly",
        hubId = "12",
        url = "http://hub:7340/analysis/12.html",
        total = 9,
        shown = 7,
        counts = mapOf("unchanged" to 5, "moved" to 2, "changed" to 2, "unverified" to 0),
    )

    private fun row(label: String, value: String) =
        "<tr><td valign=\"top\"><b>$label</b>&nbsp;&nbsp;</td><td>$value</td></tr>"

    @Test
    fun `shows the analysis and its warnings`() {
        val html = StatusHtml.render(loaded, palette)

        assertContains(html, row("Analysis", "nightly&nbsp;&nbsp;<font color=\"#muted\">#12</font>"))
        assertContains(html, row("Hub", "http://hub:7340&nbsp;&nbsp;project test_project"))
        assertContains(html, row("Page", "<a href=\"http://hub:7340/analysis/12.html\">http://hub:7340/analysis/12.html</a>"))
        assertContains(html, row("Warnings", "<b>7</b> of <b>9</b> shown"))
        assertContains(
            html,
            row(
                "",
                "<font color=\"#unchanged\">5 unchanged</font>&nbsp;&nbsp;<font color=\"#moved\">2 moved</font>" +
                    "&nbsp;&nbsp;<font color=\"#changed\">2 changed</font>",
            ),
        )
        assertContains(html, row("Presets", "<font color=\"#accent\">security</font>, <font color=\"#accent\">cert</font>"))
        assertContains(html, row("Config", "/ws/codesonar.conf"))
    }

    @Test
    fun `shows how analyses are run without an analysis`() {
        val html = StatusHtml.render(Status(hub = "http://hub:7340"), palette)

        assertContains(html, row("Analysis", "<font color=\"#muted\">none loaded</font>"))
        assertContains(html, row("Hub", "http://hub:7340"))
        assertContains(html, row("Presets", "<font color=\"#muted\">none</font>"))
        assertContains(html, row("Config", "<font color=\"#muted\">no codesonar.conf</font>"))
        assertFalse("Warnings" in html)
        assertFalse("Page" in html)
    }

    @Test
    fun `hints at the hub page`() {
        assertEquals("o open in hub  ·  q close", StatusHtml.hints(loaded))
        assertEquals("q close", StatusHtml.hints(Status()))
    }
}

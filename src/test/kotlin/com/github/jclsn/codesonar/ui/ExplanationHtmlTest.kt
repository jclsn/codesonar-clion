package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.model.ExplanationTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExplanationHtmlTest {
    private val palette = Palette("#accent", "#changed", "#muted", "#unchanged", "#moved")
    private val explanations = ExplanationTest.explanations()
    private val html = ExplanationHtml.render(explanations.take(1), palette)

    @Test
    fun `shows the warning class and its id`() {
        assertContains(html, "<b><big>Null Pointer Dereference</big></b>")
        assertContains(html, "<font color=\"#accent\">LANG.MEM.NPD</font>")
    }

    @Test
    fun `notes changed code`() {
        assertContains(html, "<font color=\"#changed\">${ExplanationHtml.CHANGED_NOTE}</font>")
        assertFalse(ExplanationHtml.CHANGED_NOTE in ExplanationHtml.render(explanations.drop(1), palette))
    }

    @Test
    fun `shows the message and the description`() {
        assertContains(html, "<p><code>p</code> is dereferenced by printf() but may be <b>NULL</b>.</p>")
        assertContains(html, "<p>See <a href=\"http://hub:7340/install/doc/NPD.html\">the manual</a>.</p>")
    }

    @Test
    fun `lists the events with their numbers and call depth`() {
        assertContains(html, "<p><b>Events</b></p>")
        assertContains(
            html,
            "<td align=\"right\" valign=\"top\">1</td><td valign=\"top\" nowrap>" +
                "<a href=\"codesonar-location:12:file:///ws/src/main.c\">main.c:12</a></td>" +
                "<td>malloc() returns NULL</td>",
        )
        assertContains(
            html,
            "<td align=\"right\" valign=\"top\"></td><td valign=\"top\" nowrap>&nbsp;&nbsp;" +
                "<a href=\"codesonar-location:7:file:///ws/src/utils.c\">utils.c:7</a></td><td>p &lt;= 0</td>",
        )
    }

    @Test
    fun `lists related places`() {
        assertContains(html, "<p><b>Related</b></p>")
        assertContains(html, "<a href=\"codesonar-location:7:file:///ws/src/utils.c\">utils.c:7</a></td><td>p is assigned here</td>")
    }

    @Test
    fun `tells violated rules from symptoms`() {
        assertContains(
            html,
            "<p><b>May violate</b></p><table><tr><td valign=\"top\" nowrap>" +
                "<a href=\"https://cwe.mitre.org/data/definitions/476.html\">CWE:476</a></td>" +
                "<td>NULL Pointer Dereference</td></tr></table>",
        )
        assertContains(
            html,
            "<p><b>May be a symptom of an earlier violation of</b></p>" +
                "<p><font color=\"#accent\">CERT-C:EXP34-C</font></p>",
        )
    }

    @Test
    fun `calls rules without relationships just rules`() {
        val plain = ExplanationHtml.render(explanations.drop(1), palette)

        assertContains(plain, "<p><b>Rules</b></p><p><font color=\"#accent\">MISRA:1.3</font></p>")
        assertFalse("Events" in plain)
        assertFalse("Related" in plain)
        assertFalse("Documentation" in plain)
    }

    @Test
    fun `links the documentation and the hub`() {
        assertContains(html, "<td><b>Documentation</b></td><td><a href=\"http://hub:7340/install/doc/NPD.html\">")
        assertContains(html, "<td><b>Hub</b></td><td><a href=\"http://hub:7340/warninginstance/42.html\">")
    }

    @Test
    fun `separates the warnings of a line`() {
        assertTrue("<hr>" in ExplanationHtml.render(explanations, palette))
        assertFalse("<hr>" in html)
    }

    @Test
    fun `hints at the keys that have a link`() {
        assertEquals("o open in hub  ·  d documentation  ·  q close", ExplanationHtml.hints(explanations))
        assertEquals("q close", ExplanationHtml.hints(explanations.drop(1)))
    }
}

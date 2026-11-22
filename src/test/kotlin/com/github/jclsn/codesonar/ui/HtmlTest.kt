package com.github.jclsn.codesonar.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HtmlTest {
    @Test
    fun `escapes text`() {
        assertEquals("a &lt; b &amp;&amp; c &gt; &quot;d&quot;", Html.escape("a < b && c > \"d\""))
    }

    @Test
    fun `turns the markdown of the server into html`() {
        assertEquals(
            "<code>p</code> may be <b>NULL</b>, see <a href=\"http://hub/doc(1).html\">the manual</a>.",
            Html.inline("`p` may be **NULL**, see [the manual](http://hub/doc(1).html)."),
        )
        assertEquals("buf[i] &lt; n (always)", Html.inline("buf[i] < n (always)"))
    }

    @Test
    fun `keeps the lines of paragraphs`() {
        assertEquals("<p>one<br>two</p>", Html.paragraph("one\ntwo\n"))
    }

    @Test
    fun `links to places in the code`() {
        val link = Html.location("file:///ws/main.c", 16, "main.c:16")

        assertEquals("<a href=\"codesonar-location:16:file:///ws/main.c\">main.c:16</a>", link)
        assertEquals(16 to "file:///ws/main.c", Html.parseLocation("codesonar-location:16:file:///ws/main.c"))
        assertNull(Html.parseLocation("http://hub:7340/warning.html"))
    }

    @Test
    fun `shortens the home directory`() {
        assertEquals("~/src/app/codesonar.conf", Html.tilde("/home/alice/src/app/codesonar.conf", "/home/alice"))
        assertEquals("/home/alice2/app", Html.tilde("/home/alice2/app", "/home/alice"))
    }
}

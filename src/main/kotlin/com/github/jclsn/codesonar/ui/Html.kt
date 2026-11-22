package com.github.jclsn.codesonar.ui

/** The colors of the popups, as HTML colors like `#cc7832`. */
data class Palette(
    /** Ids of warning classes and rules, presets. */
    val accent: String,
    /** Notes on changed code. */
    val changed: String,
    /** Missing values and hints. */
    val muted: String,
    val unchanged: String,
    val moved: String,
)

internal object Html {
    /** Links to places in the code: `codesonar-location:<1-based line>:<uri>`. */
    const val LOCATION_SCHEME = "codesonar-location:"

    private val LINK = Regex("""\[([^\]]+)]\(((?:[^()\s]|\([^()\s]*\))*)\)""")
    private val CODE = Regex("`([^`]+)`")
    private val BOLD = Regex("""\*\*([^*]+)\*\*""")

    fun escape(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    /** Text of the server as HTML, with its Markdown links, `code` and **bold**. */
    fun inline(text: String): String {
        var html = escape(text)
        html = LINK.replace(html) { """<a href="${it.groupValues[2]}">${it.groupValues[1]}</a>""" }
        html = CODE.replace(html) { "<code>${it.groupValues[1]}</code>" }
        html = BOLD.replace(html) { "<b>${it.groupValues[1]}</b>" }
        return html
    }

    /** Lines of text as one paragraph. */
    fun paragraph(text: String): String =
        "<p>" + text.trim('\n').split("\n").joinToString("<br>") { inline(it) } + "</p>"

    fun colored(text: String, color: String): String =
        """<font color="$color">${escape(text)}</font>"""

    fun link(text: String, url: String): String = """<a href="${escape(url)}">${escape(text)}</a>"""

    fun location(uri: String, line: Int, text: String): String =
        link(text, "$LOCATION_SCHEME$line:$uri")

    /** The line and URI of a location link, or null for other links. */
    fun parseLocation(href: String): Pair<Int, String>? {
        if (!href.startsWith(LOCATION_SCHEME)) return null

        val rest = href.removePrefix(LOCATION_SCHEME)
        val line = rest.substringBefore(':').toIntOrNull() ?: return null
        return line to rest.substringAfter(':')
    }

    fun document(body: String): String = "<html><body>$body</body></html>"

    /** A path with `~` for the home directory. */
    fun tilde(path: String, home: String = System.getProperty("user.home")): String =
        if (path == home || path.startsWith("$home/")) "~" + path.substring(home.length) else path
}

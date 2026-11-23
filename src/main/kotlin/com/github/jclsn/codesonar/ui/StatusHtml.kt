package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.model.Status

/**
 * The status as HTML: the shown analysis with its page on the hub, its
 * warnings, and the hub and presets of new analyses.
 */
object StatusHtml {
    fun render(status: Status, palette: Palette): String {
        val rows = mutableListOf<Pair<String, String>>()
        fun none(text: String) = Html.colored(text, palette.muted)

        rows += "Analysis" to if (status.loaded) {
            Html.escape(status.name.orEmpty()) +
                status.hubId?.let { "&nbsp;&nbsp;" + none("#$it") }.orEmpty()
        } else {
            none("none loaded")
        }

        rows += "Hub" to (status.hub?.let { Html.escape(it) } ?: none("none")) +
            status.project?.let { "&nbsp;&nbsp;project " + Html.escape(it) }.orEmpty()

        if (status.loaded) {
            status.url?.let { rows += "Page" to Html.link(it, it) }
            rows += "Warnings" to "<b>${status.shown}</b> of <b>${status.total}</b> shown"

            val counts = status.details().joinToString("&nbsp;&nbsp;") { (kind, count) ->
                Html.colored("$count $kind", color(kind, palette))
            }

            if (counts.isNotEmpty()) {
                rows += "" to counts
            }
        }

        rows += "Presets" to if (status.presets.isEmpty()) {
            none("none")
        } else {
            status.presets.joinToString(", ") { Html.colored(it, palette.accent) }
        }
        rows += "Config" to (status.conf?.let { Html.escape(Html.tilde(it)) } ?: none("no codesonar.conf"))

        return Html.document(rows.joinToString("", "<table>", "</table>") { (label, value) ->
            """<tr><td valign="top"><b>$label</b>&nbsp;&nbsp;</td><td>$value</td></tr>"""
        })
    }

    /** The keys of the popup and what they do. */
    fun hints(status: Status): String =
        listOfNotNull("o open in hub".takeIf { status.url != null }, "q close").joinToString("  ·  ")

    private fun color(kind: String, palette: Palette): String = when (kind) {
        "unchanged" -> palette.unchanged
        "moved" -> palette.moved
        "changed" -> palette.changed
        "resolved" -> palette.accent
        else -> palette.muted
    }
}

package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.model.Explanation
import com.github.jclsn.codesonar.model.uriFileName

/**
 * Explanations of warnings as HTML: the message, the events that lead to
 * the warning, numbered as on the hub, and the rules of coding standards it
 * may violate.
 */
object ExplanationHtml {
    const val CHANGED_NOTE = "The code changed since the analysis, the warning may be fixed."

    fun render(explanations: List<Explanation>, palette: Palette): String =
        Html.document(explanations.joinToString("<hr>") { explanation(it, palette) })

    /** The first link of the explanations: `hubUri` or `helpUri`. */
    fun link(explanations: List<Explanation>, select: (Explanation) -> String?): String? =
        explanations.firstNotNullOfOrNull(select)

    /** The keys of the popup and what they do. */
    fun hints(explanations: List<Explanation>): String = listOfNotNull(
        "o open in hub".takeIf { link(explanations) { it.hubUri } != null },
        "d documentation".takeIf { link(explanations) { it.helpUri } != null },
        "q close",
    ).joinToString("  ·  ")

    private fun explanation(explanation: Explanation, palette: Palette): String = buildString {
        append("<p><b><big>").append(Html.escape(explanation.title)).append("</big></b>")

        if (explanation.ruleId != null && explanation.ruleId != explanation.title) {
            append("&nbsp;&nbsp;").append(Html.colored(explanation.ruleId, palette.accent))
        }

        append("</p>")

        if (explanation.status == "changed") {
            append("<p>").append(Html.colored(CHANGED_NOTE, palette.changed)).append("</p>")
        }

        append(Html.paragraph(explanation.message))
        explanation.description?.let { append(Html.paragraph(it)) }

        val symptoms = explanation.rules.filter { it.isSymptom }
        val violations = explanation.rules.filterNot { it.isSymptom }
        // The relationships come from the hub.
        val known = explanation.rules.any { it.relationship != null }

        append(section("Events", events(explanation.events)))
        append(section("Related", related(explanation.related)))
        append(section(if (known) "May violate" else "Rules", rules(violations, palette)))
        append(section("May be a symptom of an earlier violation of", rules(symptoms, palette)))

        val links = listOfNotNull(
            explanation.helpUri?.let { "Documentation" to it },
            explanation.hubUri?.let { "Hub" to it },
        )

        if (links.isNotEmpty()) {
            append("<table>")
            links.forEach { (label, url) ->
                append("<tr><td><b>$label</b></td><td>").append(Html.link(url, url)).append("</td></tr>")
            }
            append("</table>")
        }
    }

    private fun section(heading: String, content: String): String =
        if (content.isEmpty()) "" else "<p><b>$heading</b></p>$content"

    /** The events, indented by call depth. */
    private fun events(events: List<Explanation.Event>): String {
        if (events.isEmpty()) return ""

        return events.joinToString("", "<table>", "</table>") { event ->
            val indent = "&nbsp;&nbsp;".repeat(event.depth)
            val place = Html.location(event.uri, event.line, "${uriFileName(event.uri)}:${event.line}")

            """<tr><td align="right" valign="top">${event.number ?: ""}</td>""" +
                """<td valign="top" nowrap>$indent$place</td><td>${Html.inline(event.message)}</td></tr>"""
        }
    }

    private fun related(related: List<Explanation.Location>): String {
        if (related.isEmpty()) return ""

        return related.joinToString("", "<table>", "</table>") { place ->
            val link = Html.location(place.uri, place.line, "${uriFileName(place.uri)}:${place.line}")
            """<tr><td valign="top" nowrap>$link</td><td>${Html.inline(place.message)}</td></tr>"""
        }
    }

    /** The rules with titles as a table, else their ids in one line. */
    private fun rules(rules: List<Explanation.Rule>, palette: Palette): String {
        if (rules.isEmpty()) return ""

        fun id(rule: Explanation.Rule) =
            if (rule.url != null) Html.link(rule.id, rule.url) else Html.colored(rule.id, palette.accent)

        if (rules.none { it.title != null }) {
            return "<p>" + rules.joinToString(", ") { id(it) } + "</p>"
        }

        return rules.joinToString("", "<table>", "</table>") { rule ->
            """<tr><td valign="top" nowrap>${id(rule)}</td><td>${Html.escape(rule.title.orEmpty())}</td></tr>"""
        }
    }
}

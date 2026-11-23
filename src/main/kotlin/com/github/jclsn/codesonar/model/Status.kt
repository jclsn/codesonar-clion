package com.github.jclsn.codesonar.model

import com.google.gson.JsonElement

/** The status of the shown analysis, see `codesonar.status` of the server. */
data class Status(
    val loaded: Boolean = false,
    val hidden: Boolean = false,
    val hub: String? = null,
    val project: String? = null,
    val presets: List<String> = emptyList(),
    /** The workspace configuration, if it exists. */
    val conf: String? = null,
    val name: String? = null,
    val hubId: String? = null,
    /** The page of the analysis on the hub. */
    val url: String? = null,
    val total: Int = 0,
    val shown: Int = 0,
    /** How many warnings are unchanged, moved, changed, unverified and resolved. */
    val counts: Map<String, Int> = emptyMap(),
) {
    /** The counts that are not zero, in the order of [KINDS]. */
    fun details(): List<Pair<String, Int>> =
        KINDS.mapNotNull { kind -> counts[kind]?.takeIf { it > 0 }?.let { kind to it } }

    /** One line about the analysis, e.g. for the status bar. */
    fun summary(): String {
        if (hidden) return "Hiding the analysis..."
        if (!loaded) return "No analysis loaded"

        val text = "$name: $shown of $total warnings shown"
        val details = details().joinToString(", ") { (kind, count) -> "$count $kind" }
        return if (details.isEmpty()) text else "$text ($details)"
    }

    companion object {
        val KINDS = listOf("unchanged", "moved", "changed", "unverified", "resolved")

        fun parse(json: JsonElement?): Status {
            val value = json.objectOrNull() ?: return Status()
            val counts = value.get("counts").objectOrNull()

            return Status(
                loaded = value.bool("loaded"),
                hidden = value.bool("hidden"),
                hub = value.string("hub"),
                project = value.string("project"),
                presets = value.strings("presets"),
                conf = value.string("conf"),
                name = value.string("name"),
                hubId = value.string("hubId"),
                url = value.string("url"),
                total = value.int("total") ?: 0,
                shown = value.int("shown") ?: 0,
                counts = KINDS.associateWith { counts?.int(it) ?: 0 },
            )
        }
    }
}

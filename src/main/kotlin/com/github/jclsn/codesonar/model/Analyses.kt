package com.github.jclsn.codesonar.model

import com.google.gson.JsonElement

/** An analysis of the hub project, see `codesonar.analyses` of the server. */
data class Analysis(
    val id: Long,
    val name: String,
    val started: String,
    /** E.g. `ANALYZING` or `FINISHED`. */
    val state: String,
    /** Whether it has results to show. */
    val finished: Boolean,
    /** Whether it is the shown one. */
    var current: Boolean,
    /** Whether it was downloaded before, so that it shows up quickly. */
    var cached: Boolean,
    /** Whether it is loading: only the client knows. */
    var loading: Boolean = false,
) {
    /**
     * The analysis in a list. The shown one is marked with "*", one that is
     * loading with "~".
     */
    fun line(): String {
        val mark = if (current) "*" else if (loading) "~" else " "
        val unfinished = if (finished) "" else "  [$state]"
        val local = if (cached) "  [cached]" else ""

        return "%s %4d  %s  %s%s%s".format(mark, id, name, started, unfinished, local)
    }
}

/** The analyses of a hub project, also after one of them was deleted. */
data class Analyses(
    val project: String,
    val analyses: List<Analysis>,
    /** Whether the hub deleted the analysis of `codesonar.deleteAnalysis`. */
    val deleted: Boolean = false,
    /** Why the hub refused to. */
    val message: String? = null,
) {
    companion object {
        fun parse(json: JsonElement?): Analyses {
            val value = json.objectOrNull()

            return Analyses(
                project = value?.string("project").orEmpty(),
                analyses = value?.get("analyses").objects().mapNotNull { a ->
                    Analysis(
                        id = a.long("id") ?: return@mapNotNull null,
                        name = a.string("name").orEmpty(),
                        started = a.string("started").orEmpty(),
                        state = a.string("state").orEmpty(),
                        finished = a.bool("finished"),
                        current = a.bool("current"),
                        cached = a.bool("cached"),
                    )
                },
                deleted = value?.bool("deleted") ?: false,
                message = value?.string("message"),
            )
        }
    }
}

package com.github.jclsn.codesonar.model

import com.google.gson.JsonElement

/** A shown warning, see `codesonar.warnings` of the server. Positions are 0-based. */
data class Warning(
    val uri: String,
    val line: Int,
    val column: Int,
    val endLine: Int,
    val endColumn: Int,
    /** The severity of LSP: 1 error, 2 warning, 3 information, 4 hint. */
    val severity: Int,
    /** The warning class. */
    val code: String,
    val message: String,
    /** `unchanged`, `moved`, `changed` or `unverified`. */
    val status: String?,
    /** The page of the warning on the hub. */
    val hubUri: String?,
) {
    /** The local file, or null for other URIs. */
    val path: String? get() = uriToPath(uri)

    /** The warning in a list, e.g. `[LANG.MEM.NPD] Null Pointer Dereference`. */
    fun text(): String = if (code.isEmpty()) message else "[$code] $message"

    /** Whether the warning covers the 0-based [line] of the file at [path]. */
    fun covers(path: String, line: Int): Boolean =
        this.path == path && this.line <= line && line <= endLine

    companion object {
        fun parseList(json: JsonElement?): List<Warning> = json.objects().mapNotNull { w ->
            val range = w.get("range").objectOrNull()
            val start = range?.get("start").objectOrNull()
            val end = range?.get("end").objectOrNull()

            Warning(
                uri = w.string("uri") ?: return@mapNotNull null,
                line = start?.int("line") ?: 0,
                column = start?.int("character") ?: 0,
                endLine = end?.int("line") ?: start?.int("line") ?: 0,
                endColumn = end?.int("character") ?: 0,
                severity = w.int("severity") ?: 2,
                code = w.string("code").orEmpty(),
                message = w.string("message").orEmpty(),
                status = w.string("status"),
                hubUri = w.string("hubUri"),
            )
        }
    }
}

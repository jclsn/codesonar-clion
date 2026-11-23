package com.github.jclsn.codesonar.model

import com.google.gson.JsonElement
import com.google.gson.JsonObject

/** How a warning is resolved on the hub, see `codesonar.resolve` of the server. */
enum class Resolution(val id: String) {
    /** The code was fixed: the state Fixed. */
    FIXED("fixed"),

    /** The warning is wrong: the finding False Positive. */
    FALSE_POSITIVE("falsePositive"),
}

/** The outcome of resolving warnings: how many, and what to tell the user. */
data class ResolveResult(val resolved: Int = 0, val message: String = "") {
    companion object {
        fun parse(json: JsonElement?): ResolveResult {
            val value = json.objectOrNull() ?: return ResolveResult()

            return ResolveResult(value.int("resolved") ?: 0, value.string("message").orEmpty())
        }
    }
}

/** The arguments of `codesonar.resolve`. */
object ResolveArgument {
    /** The warnings in a line of a file. */
    fun line(uri: String, line: Int, resolution: Resolution): JsonObject = JsonObject().apply {
        addProperty("uri", uri)
        add("range", JsonObject().apply {
            add("start", position(line))
            add("end", position(line + 1))
        })
        addProperty("resolution", resolution.id)
    }

    /** The argument of a code action, with the outcome left to the client to show. */
    fun quiet(argument: Any?): JsonObject =
        (Json.tree(argument).objectOrNull()?.deepCopy() ?: JsonObject()).apply { addProperty("quiet", true) }

    private fun position(line: Int) = JsonObject().apply {
        addProperty("line", line)
        addProperty("character", 0)
    }
}

package com.github.jclsn.codesonar.model

import com.google.gson.JsonElement
import com.google.gson.JsonObject

/** The outcome of `codesonar.disableCheck`: whether it worked, and what to tell the user. */
data class DisableCheckResult(val disabled: Boolean = false, val message: String = "") {
    companion object {
        fun parse(json: JsonElement?): DisableCheckResult {
            val value = json.objectOrNull() ?: return DisableCheckResult()

            return DisableCheckResult(value.bool("disabled"), value.string("message").orEmpty())
        }
    }
}

/** The arguments of `codesonar.disableCheck`. */
object DisableCheckArgument {
    /** The check of a warning class, e.g. the code of a warning. */
    fun of(checkClass: String): JsonObject = JsonObject().apply { addProperty("class", checkClass) }

    /** The classes of the warnings in the 0-based [line] of the file, each once. */
    fun classes(warnings: List<Warning>, path: String?, line: Int?): List<String> {
        if (path == null || line == null) return emptyList()

        return warnings.filter { it.code.isNotEmpty() && it.covers(path, line) }.map { it.code }.distinct()
    }
}

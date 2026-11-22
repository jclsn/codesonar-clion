package com.github.jclsn.codesonar.model

import com.google.gson.JsonElement

/** The command line of an analysis, see `codesonar.analyzeCommand`. */
data class AnalyzeCommand(
    val command: List<String>,
    val cwd: String,
    /** Files to delete once the analysis is done, e.g. one with the hub password. */
    val temporaryFiles: List<String>,
) {
    companion object {
        fun parse(json: JsonElement?): AnalyzeCommand? {
            val value = json.objectOrNull() ?: return null
            val command = value.strings("command")

            if (command.isEmpty()) return null

            return AnalyzeCommand(command, value.string("cwd").orEmpty(), value.strings("temporaryFiles"))
        }
    }
}

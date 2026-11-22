package com.github.jclsn.codesonar.model

import com.google.gson.JsonElement

/** A preset of the CodeSonar installation, e.g. `misrac2025`. */
data class Preset(val name: String, val description: String?, val selected: Boolean) {
    companion object {
        fun parseList(json: JsonElement?): List<Preset> = json.objects().mapNotNull { p ->
            Preset(p.string("name") ?: return@mapNotNull null, p.string("description"), p.bool("selected"))
        }
    }
}

/** A check (warning class) of the CodeSonar installation. */
data class Check(
    val name: String,
    val language: String,
    /** Whether CodeSonar has it on by default. */
    val default: Boolean,
    val enabled: Boolean,
) {
    companion object {
        fun parseList(json: JsonElement?): List<Check> = json.objects().mapNotNull { c ->
            Check(
                name = c.string("class") ?: return@mapNotNull null,
                language = c.string("language").orEmpty(),
                default = c.bool("default"),
                enabled = c.bool("enabled"),
            )
        }
    }
}

/** The presets and checks of analyses, see `codesonar.analysisOptions`. */
data class AnalysisOptions(val conf: String, val presets: List<Preset>, val checks: List<Check>) {
    companion object {
        fun parse(json: JsonElement?): AnalysisOptions {
            val value = json.objectOrNull()

            return AnalysisOptions(
                conf = value?.string("conf").orEmpty(),
                presets = Preset.parseList(value?.get("presets")),
                checks = Check.parseList(value?.get("checks")),
            )
        }
    }
}

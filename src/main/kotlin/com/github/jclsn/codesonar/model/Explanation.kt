package com.github.jclsn.codesonar.model

import com.google.gson.JsonElement

/** The explanation of a warning, see `codesonar.explain` of the server. */
data class Explanation(
    /** The warning class. */
    val title: String,
    val ruleId: String?,
    val message: String,
    val description: String?,
    /** `changed` if the code changed since the analysis. */
    val status: String?,
    val related: List<Location>,
    /** The events on the path to the warning. */
    val events: List<Event>,
    /** The rules of coding standards the warning may violate. */
    val rules: List<Rule>,
    val helpUri: String?,
    val hubUri: String?,
) {
    /** A place in the code, with a 1-based line. */
    data class Location(val uri: String, val line: Int, val message: String)

    /** An event, numbered as on the hub and with its call depth. */
    data class Event(val number: Int?, val uri: String, val line: Int, val message: String, val depth: Int)

    data class Rule(val id: String, val title: String?, val relationship: String?, val url: String?) {
        /** Whether the warning may be a symptom of an earlier violation of the rule. */
        val isSymptom: Boolean get() = relationship == "follows"
    }

    companion object {
        fun parseList(json: JsonElement?): List<Explanation> = json.objects().map { e ->
            Explanation(
                title = e.string("title") ?: "Warning",
                ruleId = e.string("ruleId"),
                message = e.string("message").orEmpty(),
                description = e.string("description"),
                status = e.string("status"),
                related = e.get("related").objects().map { r ->
                    Location(r.string("uri").orEmpty(), r.int("line") ?: 0, r.string("message").orEmpty())
                },
                events = e.get("events").objects().map { v ->
                    Event(
                        number = v.int("number"),
                        uri = v.string("uri").orEmpty(),
                        line = v.int("line") ?: 0,
                        message = v.string("message").orEmpty(),
                        depth = v.int("depth") ?: 0,
                    )
                },
                rules = e.get("rules").objects().mapNotNull { r ->
                    Rule(
                        id = r.string("id") ?: return@mapNotNull null,
                        title = r.string("title"),
                        relationship = r.string("relationship"),
                        url = r.string("url"),
                    )
                },
                helpUri = e.string("helpUri"),
                hubUri = e.string("hubUri"),
            )
        }
    }
}

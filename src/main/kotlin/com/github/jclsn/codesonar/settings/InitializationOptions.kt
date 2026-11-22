package com.github.jclsn.codesonar.settings

import com.google.gson.JsonObject
import com.google.gson.JsonParser

/** Builds the settings of the server, its `initializationOptions`. */
object InitializationOptions {
    /**
     * The fields of the settings page, on top of the additional settings:
     * a field that is set wins over the same setting in the JSON.
     */
    fun build(state: CodeSonarSettings.State, password: String?): JsonObject {
        val options = parseAdditional(state.additionalSettings) ?: JsonObject()
        val hub = child(options, "hub")

        set(hub, "url", state.hubUrl)
        set(hub, "username", state.username)
        set(hub, "password", password.orEmpty())
        set(hub, "passwordFile", state.passwordFile)
        set(hub, "project", state.hubProject)
        set(options, "sarifFile", state.sarifFile)

        if (state.markChanged) {
            options.addProperty("changed", "mark")
        }

        if (hub.size() == 0) {
            options.remove("hub")
        }

        return options
    }

    /** The additional settings, or null if they are not a JSON object. */
    fun parseAdditional(text: String): JsonObject? {
        if (text.isBlank()) return JsonObject()

        return try {
            JsonParser.parseString(text) as? JsonObject
        } catch (_: RuntimeException) {
            null
        }
    }

    private fun child(parent: JsonObject, key: String): JsonObject {
        val existing = parent.get(key) as? JsonObject
        if (existing != null) return existing

        return JsonObject().also { parent.add(key, it) }
    }

    private fun set(target: JsonObject, key: String, value: String) {
        if (value.isNotBlank()) {
            target.addProperty(key, value.trim())
        }
    }
}


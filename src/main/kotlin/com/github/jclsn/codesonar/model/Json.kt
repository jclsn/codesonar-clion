package com.github.jclsn.codesonar.model

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject

/** Lenient access to the JSON of the server: missing values are null. */
internal object Json {
    private val gson = Gson()

    /** The JSON of a result of lsp4j, which may be a tree or plain objects. */
    fun tree(value: Any?): JsonElement = when (value) {
        null -> JsonNull.INSTANCE
        is JsonElement -> value
        else -> gson.toJsonTree(value)
    }
}

internal fun JsonElement?.objectOrNull(): JsonObject? = this as? JsonObject

internal fun JsonElement?.elements(): List<JsonElement> = (this as? JsonArray)?.toList().orEmpty()

internal fun JsonElement?.objects(): List<JsonObject> = elements().filterIsInstance<JsonObject>()

private fun JsonObject.primitive(key: String) = get(key)?.takeIf { it.isJsonPrimitive }?.asJsonPrimitive

/** A string, or a number as a string: ids are numbers on some hubs. */
internal fun JsonObject.string(key: String): String? = primitive(key)?.asString

internal fun JsonObject.bool(key: String): Boolean =
    primitive(key)?.takeIf { it.isBoolean }?.asBoolean ?: false

internal fun JsonObject.int(key: String): Int? =
    primitive(key)?.takeIf { it.isNumber }?.asInt

internal fun JsonObject.long(key: String): Long? =
    primitive(key)?.takeIf { it.isNumber }?.asLong

internal fun JsonObject.strings(key: String): List<String> =
    get(key).elements().filter { it.isJsonPrimitive }.map { it.asString }

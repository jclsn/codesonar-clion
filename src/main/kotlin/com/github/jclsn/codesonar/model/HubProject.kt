package com.github.jclsn.codesonar.model

import com.google.gson.JsonElement

/** A project of the hub. */
data class HubProject(val id: String, val name: String) {
    companion object {
        fun parseList(json: JsonElement?): List<HubProject> =
            json.objects().mapNotNull { p ->
                HubProject(p.string("id").orEmpty(), p.string("name") ?: return@mapNotNull null)
            }
    }
}

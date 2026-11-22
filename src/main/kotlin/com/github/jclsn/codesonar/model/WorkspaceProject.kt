package com.github.jclsn.codesonar.model

import com.google.gson.JsonElement

/** The workspace configuration, see `codesonar.project` of the server. */
data class WorkspaceProject(
    /** The `codesonar.conf` of the workspace. */
    val workspaceConf: String,
    val workspaceConfExists: Boolean,
    /** Whether it has the section of codesonar-lsp. */
    val section: Boolean,
    /** What the configuration could start with. */
    val suggested: Suggested,
) {
    data class Suggested(
        val project: String,
        val build: String,
        val incremental: Boolean,
        val incrementalBuild: String,
        val hubAddress: String,
    )

    companion object {
        fun parse(json: JsonElement?): WorkspaceProject? {
            val value = json.objectOrNull() ?: return null
            val suggested = value.get("suggested").objectOrNull()

            return WorkspaceProject(
                workspaceConf = value.string("workspaceConf") ?: return null,
                workspaceConfExists = value.bool("workspaceConfExists"),
                section = value.bool("section"),
                suggested = Suggested(
                    project = suggested?.string("project").orEmpty(),
                    build = suggested?.string("build").orEmpty(),
                    incremental = suggested?.get("incremental")?.takeIf { it.isJsonPrimitive }?.asBoolean ?: true,
                    incrementalBuild = suggested?.string("incrementalBuild").orEmpty(),
                    hubAddress = suggested?.string("hubAddress").orEmpty(),
                ),
            )
        }
    }
}

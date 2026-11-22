package com.github.jclsn.codesonar.lsp

import com.google.gson.JsonObject
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification

/** Receives the notifications that codesonar-lsp adds to LSP. */
class CodeSonarLanguageClient(
    handler: LspServerNotificationsHandler,
    private val listener: Listener,
) : Lsp4jClient(handler) {
    interface Listener {
        /** An analysis starts loading: `{analysis}`, `{project}` or `{file}`. */
        fun loading(params: JsonObject)

        /** An analysis was shown; [params] is the status. */
        fun loaded(params: JsonObject)
    }

    @JsonNotification("codesonar/loading")
    fun loading(params: JsonObject) = listener.loading(params)

    @JsonNotification("codesonar/loaded")
    fun loaded(params: JsonObject) = listener.loaded(params)
}

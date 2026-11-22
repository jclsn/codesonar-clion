package com.github.jclsn.codesonar.settings

import com.github.jclsn.codesonar.lsp.ServerExecutable
import com.google.gson.JsonObject
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

/**
 * The settings of the plugin, for all projects: what is specific to a
 * project lives in its `codesonar.conf`, so these are mostly the server
 * and the hub credentials.
 */
@Service(Service.Level.APP)
@State(name = "CodeSonarSettings", storages = [Storage("codesonar.xml")])
class CodeSonarSettings : PersistentStateComponent<CodeSonarSettings.State> {
    data class State(
        /** The server: a path, or a name in `$PATH`. */
        var serverPath: String = ServerExecutable.DEFAULT,
        var hubUrl: String = "",
        var username: String = "",
        var passwordFile: String = "",
        var hubProject: String = "",
        var sarifFile: String = "",
        /** Show warnings in changed code as hints instead of hiding them. */
        var markChanged: Boolean = false,
        /** Further settings of the server, a JSON object. */
        var additionalSettings: String = "",
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    /** The hub password, which the password store of the IDE keeps. */
    var password: String?
        get() = PasswordSafe.instance.getPassword(CREDENTIALS)
        set(value) = PasswordSafe.instance.setPassword(CREDENTIALS, value?.takeIf { it.isNotEmpty() })

    fun initializationOptions(): JsonObject = InitializationOptions.build(state, password)

    companion object {
        private val CREDENTIALS = CredentialAttributes(generateServiceName("CodeSonar", "hub"))

        fun getInstance(): CodeSonarSettings = service()
    }
}

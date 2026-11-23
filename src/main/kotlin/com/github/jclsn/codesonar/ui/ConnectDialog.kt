package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.settings.CodeSonarSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

/**
 * Asks for the hub to sign in to. Without a URL, the configured hub is used.
 * The hub, the user and the password start as the ones of the settings,
 * and are kept there: the server forgets them when it stops, and the
 * workspace configuration keeps the address of the hub without `https`.
 */
class ConnectDialog(
    project: Project,
    private val settings: CodeSonarSettings = CodeSonarSettings.getInstance(),
) : DialogWrapper(project) {
    private val url = JBTextField(settings.state.hubUrl, 32).apply { emptyText.text = "The configured hub" }
    private val username = JBTextField(settings.state.username, 32).apply { emptyText.text = "The configured user" }
    private val password = JBPasswordField().apply { text = settings.password.orEmpty() }
    private val remember = JBCheckBox("Remember the hub, the user and the password", true)

    init {
        title = "Connect to CodeSonar Hub"
        setOKButtonText("Connect")
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Hub URL:") { cell(url).align(AlignX.FILL) }
        row("User:") { cell(username).align(AlignX.FILL) }
        row("Password:") { cell(password).align(AlignX.FILL) }
        row { cell(remember).comment("In Settings | Tools | CodeSonar and the password store of the IDE") }
    }

    override fun getPreferredFocusedComponent(): JComponent = url

    /** The argument of `codesonar.connect`. */
    fun argument(): Map<String, String> =
        argument(url.text, username.text, String(password.password))

    /** Keeps what was entered for the next start of the IDE. */
    fun remember() {
        if (!remember.isSelected) return

        if (url.text.isNotBlank()) settings.state.hubUrl = url.text.trim()

        if (username.text.isNotBlank()) {
            settings.state.username = username.text.trim()
            settings.password = String(password.password)
        }
    }

    companion object {
        /** Only what was entered; a password belongs to a user. */
        fun argument(url: String, username: String, password: String): Map<String, String> = buildMap {
            if (url.isNotBlank()) put("url", url.trim())

            if (username.isNotBlank()) {
                put("username", username.trim())
                put("password", password)
            }
        }
    }
}

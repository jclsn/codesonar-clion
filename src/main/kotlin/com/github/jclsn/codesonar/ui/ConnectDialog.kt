package com.github.jclsn.codesonar.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

/** Asks for the hub to sign in to. Without a URL, the configured hub is used. */
class ConnectDialog(project: Project) : DialogWrapper(project) {
    private val url = JBTextField(32).apply { emptyText.text = "The configured hub" }
    private val username = JBTextField(32).apply { emptyText.text = "The configured user" }
    private val password = JBPasswordField()

    init {
        title = "Connect to CodeSonar Hub"
        setOKButtonText("Connect")
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Hub URL:") { cell(url).align(AlignX.FILL) }
        row("User:") { cell(username).align(AlignX.FILL) }
        row("Password:") { cell(password).align(AlignX.FILL) }
    }

    override fun getPreferredFocusedComponent(): JComponent = url

    /** The argument of `codesonar.connect`. */
    fun argument(): Map<String, String> =
        argument(url.text, username.text, String(password.password))

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

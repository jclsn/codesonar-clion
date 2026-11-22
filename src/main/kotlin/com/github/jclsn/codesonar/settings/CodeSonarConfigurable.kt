package com.github.jclsn.codesonar.settings

import com.github.jclsn.codesonar.lsp.CodeSonarLspIntegrationProvider
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.rows

/** The settings page: Settings | Tools | CodeSonar. */
class CodeSonarConfigurable : BoundConfigurable("CodeSonar") {
    private val settings = CodeSonarSettings.getInstance()
    private val state get() = settings.state
    private var password = ""

    override fun createPanel(): DialogPanel {
        password = settings.password.orEmpty()

        return panel {
            group("Language Server") {
                row("Server:") {
                    textFieldWithBrowseButton(FileChooserDescriptorFactory.singleFile())
                        .bindText(state::serverPath)
                        .align(AlignX.FILL)
                        .comment("The path of codesonar-lsp, or its name in \$PATH or ~/.cargo/bin")
                }
            }
            group("Hub") {
                row("URL:") {
                    textField().bindText(state::hubUrl).align(AlignX.FILL)
                        .comment("Only without HUB_ADDRESS in the codesonar.conf of the workspace, or to use https")
                }
                row("User:") {
                    textField().bindText(state::username).align(AlignX.FILL)
                        .comment("Empty for anonymous access")
                }
                row("Password:") {
                    passwordField().bindText(::password).align(AlignX.FILL)
                        .comment("Kept in the password store of the IDE")
                }
                row("Password file:") {
                    textFieldWithBrowseButton(FileChooserDescriptorFactory.singleFile())
                        .bindText(state::passwordFile)
                        .align(AlignX.FILL)
                        .comment("Instead of the password. Without either, \$CODESONAR_PASSWORD is used")
                }
                row("Project:") {
                    textField().bindText(state::hubProject).align(AlignX.FILL)
                        .comment("Only if the codesonar.conf of the workspace names none")
                }
            }
            group("Warnings") {
                row("SARIF file:") {
                    textField().bindText(state::sarifFile).align(AlignX.FILL)
                        .comment("Shown instead of a hub, relative to the workspace root")
                }
                row {
                    checkBox("Show warnings in code changed since the analysis as hints")
                        .bindSelected(state::markChanged)
                        .comment("They are hidden otherwise, since they are usually fixed")
                }
            }
            group("Additional Settings") {
                row {
                    textArea().bindText(state::additionalSettings).rows(6).align(AlignX.FILL)
                        .comment(
                            "Further settings of codesonar-lsp as a JSON object, e.g. " +
                                "{\"cachedAnalyses\": 5, \"analyze\": {\"buildCommand\": \"make -B\"}}",
                        )
                        .validationOnInput {
                            if (InitializationOptions.parseAdditional(it.text) == null) error("Not a JSON object") else null
                        }
                }
            }
        }
    }

    override fun isModified(): Boolean = super.isModified() || password != settings.password.orEmpty()

    override fun apply() {
        super.apply()
        settings.password = password

        // The servers take the settings when they start.
        for (project in ProjectManager.getInstance().openProjects) {
            LspClientManager.getInstance(project)
                .stopAndRestartClientsIfNeeded(CodeSonarLspIntegrationProvider::class.java)
        }
    }
}

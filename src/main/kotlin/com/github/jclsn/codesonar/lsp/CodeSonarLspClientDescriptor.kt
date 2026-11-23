package com.github.jclsn.codesonar.lsp

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.Commands
import com.github.jclsn.codesonar.actions.ExplainAction
import com.github.jclsn.codesonar.actions.ResolveAction
import com.github.jclsn.codesonar.settings.CodeSonarSettings
import com.google.gson.JsonObject
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.LspClientDescriptor
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.platform.lsp.api.customization.LspCommandsCustomizer
import com.intellij.platform.lsp.api.customization.LspCommandsSupport
import com.intellij.platform.lsp.api.customization.LspCustomization
import org.eclipse.lsp4j.Command
import java.nio.file.Path

/** Starts codesonar-lsp for the C and C++ files of a workspace. */
class CodeSonarLspClientDescriptor(
    project: Project,
    private val root: VirtualFile,
    private val executable: Path,
) : LspClientDescriptor(project, "CodeSonar", root) {
    override fun isSupportedFile(file: VirtualFile): Boolean =
        SourceFiles.isSource(file.name) && VfsUtilCore.isAncestor(root, file, false)

    override fun createCommandLine(): GeneralCommandLine =
        GeneralCommandLine(executable.toString()).withWorkDirectory(root.path)

    override fun createInitializationOptions(): Any =
        CodeSonarSettings.getInstance().initializationOptions()

    override fun createLsp4jClient(handler: LspServerNotificationsHandler): Lsp4jClient =
        CodeSonarLanguageClient(handler, object : CodeSonarLanguageClient.Listener {
            override fun loading(params: JsonObject) {
                if (!project.isDisposed) CodeSonarService.getInstance(project).onLoading(params)
            }

            override fun loaded(params: JsonObject) {
                if (!project.isDisposed) CodeSonarService.getInstance(project).onLoaded(params)
            }
        })

    override val lspCustomization: LspCustomization = object : LspCustomization() {
        override val commandsCustomizer: LspCommandsCustomizer = object : LspCommandsSupport() {
            // The explain code action is shown here instead of in the server,
            // and the outcome of resolving warnings like the other messages.
            override fun executeCommand(lspClient: LspClient, contextFile: VirtualFile, command: Command) {
                val argument = command.arguments?.firstOrNull()

                when (command.command) {
                    Commands.EXPLAIN -> onEdt {
                        val editor = FileEditorManager.getInstance(project).selectedTextEditor
                            ?.takeIf { it.virtualFile == contextFile }
                        ExplainAction.explain(project, editor, argument)
                    }

                    Commands.RESOLVE -> onEdt { ResolveAction.resolve(project, argument) }
                    else -> super.executeCommand(lspClient, contextFile, command)
                }
            }

            private fun onEdt(action: () -> Unit) = ApplicationManager.getApplication().invokeLater {
                if (!project.isDisposed) action()
            }
        }
    }
}

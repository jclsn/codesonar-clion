package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.Commands
import com.github.jclsn.codesonar.Notifier
import com.github.jclsn.codesonar.model.WorkspaceProject
import com.github.jclsn.codesonar.ui.WorkspaceConfDialog
import com.google.gson.JsonElement
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

/**
 * Opens the configuration of the workspace, its `codesonar.conf`. Without
 * the section of codesonar-lsp in it, a form asks for the basic settings
 * first.
 */
class ConfAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        val open = { result: JsonElement ->
            val workspace = WorkspaceProject.parse(result)

            if (workspace == null) {
                Notifier.error(project, "The server sent no workspace configuration")
            } else {
                service.openFile(workspace.workspaceConf)
            }
        }

        service.execute(Commands.PROJECT) { result ->
            val workspace = WorkspaceProject.parse(result)

            if (workspace == null || workspace.section) {
                open(result)
                return@execute
            }

            val dialog = WorkspaceConfDialog(project, workspace)

            if (dialog.showAndGet()) {
                service.execute(Commands.INIT_CONF, dialog.argument(), done = open)
            }
        }
    }
}

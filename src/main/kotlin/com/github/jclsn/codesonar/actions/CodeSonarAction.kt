package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.CodeSonarService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project

/**
 * An action of the plugin. The actions need a running server, so they are
 * used with a file of the workspace open.
 */
abstract class CodeSonarAction : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project != null
    }

    final override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        perform(project, CodeSonarService.getInstance(project), event)
    }

    protected abstract fun perform(project: Project, service: CodeSonarService, event: AnActionEvent)
}

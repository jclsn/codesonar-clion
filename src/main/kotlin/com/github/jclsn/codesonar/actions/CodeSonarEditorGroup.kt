package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.CodeSonarService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup

/** The actions in the context menu of editors, where a server is running. */
class CodeSonarEditorGroup : DefaultActionGroup() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val project = event.project

        event.presentation.isEnabledAndVisible =
            project != null && CodeSonarService.getInstance(project).clients().isNotEmpty()
    }
}

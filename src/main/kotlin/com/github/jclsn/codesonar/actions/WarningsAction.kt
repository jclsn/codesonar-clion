package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.ui.WarningsToolWindowFactory
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

/** Lists the shown warnings in a tool window. */
class WarningsAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        WarningsToolWindowFactory.show(project)
    }
}

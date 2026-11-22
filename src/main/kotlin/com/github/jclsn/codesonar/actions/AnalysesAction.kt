package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.Commands
import com.github.jclsn.codesonar.model.Analyses
import com.github.jclsn.codesonar.ui.AnalysesPopup
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

/** Chooses, shows, hides and deletes the analyses of the hub project. */
class AnalysesAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        service.execute(Commands.ANALYSES) { AnalysesPopup(project, Analyses.parse(it)).show() }
    }
}

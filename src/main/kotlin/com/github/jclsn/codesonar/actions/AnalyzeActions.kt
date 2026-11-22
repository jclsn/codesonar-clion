package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.AnalysisRunner
import com.github.jclsn.codesonar.CodeSonarService
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

/** Runs `codesonar analyze` for the workspace. */
class AnalyzeAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        AnalysisRunner.getInstance(project).analyze()
    }
}

/** Runs an analysis that gets a name on the hub. */
class AnalyzeAsAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        val name = Messages.showInputDialog(
            project,
            "Name of the analysis on the hub:",
            "CodeSonar Analyze",
            null,
        ) ?: return

        AnalysisRunner.getInstance(project).analyze(name)
    }
}

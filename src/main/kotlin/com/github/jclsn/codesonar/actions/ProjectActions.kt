package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.Commands
import com.github.jclsn.codesonar.model.HubProject
import com.github.jclsn.codesonar.ui.ConnectDialog
import com.github.jclsn.codesonar.ui.ProjectChooser
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

/** Chooses another project of the hub. */
class ProjectsAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        service.execute(Commands.PROJECTS) { ProjectChooser.show(project, HubProject.parseList(it)) }
    }
}

/** Signs in to a hub and chooses one of its projects. */
class ConnectAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        val dialog = ConnectDialog(project)
        if (!dialog.showAndGet()) return

        service.execute(Commands.CONNECT, dialog.argument()) {
            ProjectChooser.show(project, HubProject.parseList(it))
        }
    }
}

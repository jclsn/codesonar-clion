package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.Commands
import com.github.jclsn.codesonar.Notifier
import com.github.jclsn.codesonar.model.HubProject
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory

/** Chooses a project of the hub, whose newest analysis is shown then. */
object ProjectChooser {
    fun show(project: Project, projects: List<HubProject>) {
        if (projects.isEmpty()) {
            Notifier.error(project, "The hub has no projects")
            return
        }

        JBPopupFactory.getInstance().createPopupChooserBuilder(projects)
            .setTitle("CodeSonar Projects")
            .setRenderer(textRenderer { "${it.id}  ${it.name}" })
            .setNamerForFiltering { it.name }
            .setItemChosenCallback { chosen ->
                CodeSonarService.getInstance(project)
                    .execute(Commands.CONNECT, mapOf("project" to chosen.name)) {}
            }
            .createPopup()
            .showCenteredInCurrentWindow(project)
    }
}

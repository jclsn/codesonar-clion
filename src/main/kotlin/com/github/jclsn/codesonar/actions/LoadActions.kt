package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.Commands
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir

/** Shows the configured analysis. */
class LoadAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        service.execute(Commands.LOAD) {}
    }
}

/** Shows an exported SARIF file. */
class LoadSarifAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        val descriptor = FileChooserDescriptorFactory.singleFile()
            .withTitle("Load SARIF File")
            .withDescription("An exported SARIF file with CodeSonar warnings")
        val file = FileChooser.chooseFile(descriptor, project, project.guessProjectDir()) ?: return

        service.execute(Commands.LOAD, mapOf("file" to file.path)) {}
    }
}

/** Loads the shown analysis again. */
class RefreshAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        service.execute(Commands.REFRESH) {}
    }
}

/** Turns the shown analysis off, until another one is shown. */
class HideAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        service.execute(Commands.HIDE) {}
    }
}

package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.Commands
import com.github.jclsn.codesonar.model.Status
import com.github.jclsn.codesonar.ui.HtmlPopup
import com.github.jclsn.codesonar.ui.StatusHtml
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

/**
 * Shows the status in a popup: the analysis, its warnings and how analyses
 * are run.
 */
class StatusAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) = show(project)

    companion object {
        fun show(project: Project) {
            CodeSonarService.getInstance(project).execute(Commands.STATUS) { result ->
                val status = Status.parse(result)
                val keys = buildMap<Char, () -> Unit> {
                    status.url?.let { put('o') { BrowserUtil.browse(it, project) } }
                }

                HtmlPopup.show(
                    project,
                    null,
                    "CodeSonar",
                    StatusHtml.render(status, HtmlPopup.palette()),
                    StatusHtml.hints(status),
                    keys,
                )
            }
        }
    }
}

package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.Commands
import com.github.jclsn.codesonar.Notifier
import com.github.jclsn.codesonar.model.Warning
import com.github.jclsn.codesonar.ui.Browser
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project

/** Opens the warning at the caret on the hub, or else the shown analysis. */
class OpenHubAction : CodeSonarAction() {
    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR)
        val path = editor?.virtualFile?.path
        val line = editor?.caretModel?.logicalPosition?.line

        service.execute(Commands.WARNINGS) { result ->
            val url = hubUrl(Warning.parseList(result), path, line) ?: service.status.url

            if (url != null) {
                Browser.open(project, url)
            } else {
                Notifier.error(project, "No warning with a hub link at the caret")
            }
        }
    }

    companion object {
        /** The hub page of the first warning in the 0-based [line] of the file. */
        fun hubUrl(warnings: List<Warning>, path: String?, line: Int?): String? {
            if (path == null || line == null) return null

            return warnings.firstOrNull { it.hubUri != null && it.covers(path, line) }?.hubUri
        }
    }
}

package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.Commands
import com.github.jclsn.codesonar.Notifier
import com.github.jclsn.codesonar.model.Explanation
import com.github.jclsn.codesonar.ui.ExplanationPopup
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

/** Explains the warnings in the line of the caret, like the code action. */
class ExplainAction : CodeSonarAction() {
    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project != null && event.getData(CommonDataKeys.EDITOR) != null
    }

    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val file = editor.virtualFile ?: return
        val client = service.client()

        if (client == null) {
            Notifier.error(project, CodeSonarService.NOT_RUNNING)
            return
        }

        val line = editor.caretModel.logicalPosition.line
        val argument = mapOf(
            "uri" to client.descriptor.getFileUri(file),
            "range" to mapOf(
                "start" to mapOf("line" to line, "character" to 0),
                "end" to mapOf("line" to line + 1, "character" to 0),
            ),
        )

        explain(project, editor, argument)
    }

    companion object {
        /** Explains the warnings that [argument] picks, see `codesonar.explain`. */
        fun explain(project: Project, editor: Editor?, argument: Any?) {
            CodeSonarService.getInstance(project).execute(Commands.EXPLAIN, argument) { result ->
                ExplanationPopup.show(project, editor, Explanation.parseList(result))
            }
        }
    }
}

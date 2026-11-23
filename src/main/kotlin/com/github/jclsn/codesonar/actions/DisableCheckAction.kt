package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.Commands
import com.github.jclsn.codesonar.Notifier
import com.github.jclsn.codesonar.model.DisableCheckArgument
import com.github.jclsn.codesonar.model.DisableCheckResult
import com.github.jclsn.codesonar.model.ResolveArgument
import com.github.jclsn.codesonar.model.Warning
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory

/**
 * Disables the check of the warning in the line of the caret, like the
 * code action: the check is turned off in the workspace configuration for
 * later analyses, and its warnings are hidden now.
 */
class DisableCheckAction : CodeSonarAction() {
    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project != null && event.getData(CommonDataKeys.EDITOR) != null
    }

    override fun perform(project: Project, service: CodeSonarService, event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val path = editor.virtualFile?.path
        val line = editor.caretModel.logicalPosition.line

        service.execute(Commands.WARNINGS) { result ->
            val classes = DisableCheckArgument.classes(Warning.parseList(result), path, line)

            when (classes.size) {
                0 -> Notifier.error(project, "No warning in the line of the caret")
                1 -> disable(project, DisableCheckArgument.of(classes[0]))
                else -> JBPopupFactory.getInstance().createPopupChooserBuilder(classes)
                    .setTitle("Disable Check")
                    .setItemChosenCallback { disable(project, DisableCheckArgument.of(it)) }
                    .createPopup()
                    .showInBestPositionFor(editor)
            }
        }
    }

    companion object {
        /** Disables the check that [argument] names and shows the outcome of the server. */
        fun disable(project: Project, argument: Any?) {
            CodeSonarService.getInstance(project).execute(Commands.DISABLE_CHECK, ResolveArgument.quiet(argument)) {
                val result = DisableCheckResult.parse(it)

                if (result.disabled) Notifier.info(project, result.message) else Notifier.error(project, result.message)
            }
        }
    }
}

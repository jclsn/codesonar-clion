package com.github.jclsn.codesonar.actions

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.Commands
import com.github.jclsn.codesonar.Notifier
import com.github.jclsn.codesonar.model.Resolution
import com.github.jclsn.codesonar.model.ResolveArgument
import com.github.jclsn.codesonar.model.ResolveResult
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project

/**
 * Resolves the warnings in the line of the caret on the hub, like the code
 * actions. The hub keeps it for later analyses, and resolved warnings are
 * not shown any more.
 */
abstract class ResolveAction(private val resolution: Resolution) : CodeSonarAction() {
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
        resolve(project, ResolveArgument.line(client.descriptor.getFileUri(file), line, resolution))
    }

    companion object {
        /** Resolves the warnings that [argument] picks and shows the outcome of the server. */
        fun resolve(project: Project, argument: Any?) {
            CodeSonarService.getInstance(project).execute(Commands.RESOLVE, ResolveArgument.quiet(argument)) {
                val result = ResolveResult.parse(it)

                if (result.resolved > 0) Notifier.info(project, result.message) else Notifier.error(project, result.message)
            }
        }
    }
}

/** Sets the state of the warnings to Fixed. */
class ResolveFixedAction : ResolveAction(Resolution.FIXED)

/** Sets the finding of the warnings to False Positive. */
class ResolveFalsePositiveAction : ResolveAction(Resolution.FALSE_POSITIVE)

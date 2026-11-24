package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.Notifier
import com.github.jclsn.codesonar.model.Explanation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

/** Explains warnings in a popup at the caret. */
object ExplanationPopup {
    fun show(project: Project, editor: Editor?, explanations: List<Explanation>) {
        if (explanations.isEmpty()) {
            Notifier.error(project, "No warning in this line")
            return
        }

        val keys = buildMap<Char, () -> Unit> {
            ExplanationHtml.link(explanations) { it.hubUri }?.let { put('o') { Browser.open(project, it) } }
            ExplanationHtml.link(explanations) { it.helpUri }?.let { put('d') { Browser.open(project, it) } }
        }

        HtmlPopup.show(
            project,
            editor,
            "CodeSonar",
            ExplanationHtml.render(explanations, HtmlPopup.palette()),
            ExplanationHtml.hints(explanations),
            keys,
        )
    }
}

package com.github.jclsn.codesonar.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

/** The tool window with the shown warnings, which appears on request. */
class WarningsToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun shouldBeAvailable(project: Project): Boolean = false

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = WarningsPanel(project, toolWindow)
        val content = ContentFactory.getInstance().createContent(panel, "", false)

        content.setDisposer(panel)
        toolWindow.contentManager.addContent(content)
        panel.refresh()
    }

    companion object {
        const val ID = "CodeSonar"

        /** Opens the tool window with the current warnings. */
        fun show(project: Project) {
            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(ID) ?: return
            val panel = toolWindow.contentManager.contents.firstOrNull()?.component as? WarningsPanel

            toolWindow.isAvailable = true
            toolWindow.activate(null)
            // A new panel asks for the warnings itself.
            panel?.refresh()
        }
    }
}

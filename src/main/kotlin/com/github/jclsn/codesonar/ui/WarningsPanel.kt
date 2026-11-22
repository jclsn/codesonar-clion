package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.CodeSonarListener
import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.Commands
import com.github.jclsn.codesonar.model.Status
import com.github.jclsn.codesonar.model.Warning
import com.github.jclsn.codesonar.model.uriFileName
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel

/**
 * The shown warnings as a list: Enter or a double click goes to the
 * warning. The list is updated when another analysis is shown.
 */
class WarningsPanel(
    private val project: Project,
    private val toolWindow: ToolWindow,
) : JPanel(BorderLayout()), Disposable {
    private val model = CollectionListModel<Warning>()
    private val list = JBList(model)

    init {
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.cellRenderer = Renderer()
        list.emptyText.text = "No CodeSonar warnings shown"
        list.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(event: KeyEvent) {
                if (event.keyCode == KeyEvent.VK_ENTER) {
                    navigate()
                    event.consume()
                }
            }
        })
        object : DoubleClickListener() {
            override fun onDoubleClick(event: MouseEvent): Boolean {
                navigate()
                return true
            }
        }.installOn(list)
        add(ScrollPaneFactory.createScrollPane(list, true), BorderLayout.CENTER)

        project.messageBus.connect(this).subscribe(CodeSonarListener.TOPIC, object : CodeSonarListener {
            override fun loaded(status: Status) {
                ApplicationManager.getApplication().invokeLater {
                    if (!project.isDisposed && toolWindow.isAvailable) refresh()
                }
            }
        })
    }

    /** Asks the server for the shown warnings. */
    fun refresh() {
        val service = CodeSonarService.getInstance(project)

        service.execute(Commands.WARNINGS) { result ->
            model.replaceAll(Warning.parseList(result))
            toolWindow.contentManager.contents.firstOrNull()?.displayName = title(service.status)
        }
    }

    private fun navigate() {
        val warning = list.selectedValue ?: return
        val path = warning.path ?: return

        CodeSonarService.getInstance(project).openFile(path, warning.line, warning.column)
    }

    override fun dispose() {}

    private class Renderer : ColoredListCellRenderer<Warning>() {
        override fun customizeCellRenderer(
            list: JList<out Warning>,
            value: Warning,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean,
        ) {
            icon = icon(value.severity)
            append(value.text())
            append("  ${uriFileName(value.uri)}:${value.line + 1}", SimpleTextAttributes.GRAYED_ATTRIBUTES)

            if (value.status != null && value.status != "unchanged") {
                append("  ${value.status}", SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES)
            }
        }
    }

    companion object {
        /** The title of the list, with the name of the shown analysis. */
        fun title(status: Status): String = if (status.loaded) status.name.orEmpty() else ""

        /** The icon of a severity of LSP. */
        fun icon(severity: Int): Icon = when (severity) {
            1 -> AllIcons.General.Error
            2 -> AllIcons.General.Warning
            3 -> AllIcons.General.Information
            else -> AllIcons.General.Note
        }
    }
}

package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.Commands
import com.github.jclsn.codesonar.Notifier
import com.github.jclsn.codesonar.model.Analyses
import com.github.jclsn.codesonar.model.Analysis
import com.github.jclsn.codesonar.model.Status
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.CollectionListModel
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.Font
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.ListSelectionModel

/**
 * The analyses of the hub project, newest first: Enter shows one, Space
 * shows or hides one and keeps the list open, `x` deletes the local copy of
 * one and `d` deletes one from the hub.
 */
class AnalysesPopup(private val project: Project, private val result: Analyses) {
    private val service = CodeSonarService.getInstance(project)
    private val analyses = result.analyses
    private val list = JBList(CollectionListModel(analyses))
    private lateinit var popup: JBPopup

    /** Shows the list with the analysis at [index] chosen. */
    fun show(index: Int = 0) {
        if (analyses.isEmpty()) {
            Notifier.error(project, "No analyses in hub project ${result.project}")
            return
        }

        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.font = Font(Font.MONOSPACED, Font.PLAIN, list.font.size)
        list.cellRenderer = textRenderer { it.line() }
        list.selectedIndex = index.coerceIn(0, analyses.lastIndex)
        list.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(event: KeyEvent) {
                if (event.isControlDown || event.isAltDown || event.isMetaDown) return

                when (event.keyCode) {
                    KeyEvent.VK_ENTER -> choose()
                    KeyEvent.VK_SPACE -> toggle()
                    KeyEvent.VK_X -> uncache()
                    KeyEvent.VK_D -> delete()
                    KeyEvent.VK_Q -> popup.cancel()
                    else -> return
                }

                event.consume()
            }
        })
        object : DoubleClickListener() {
            override fun onDoubleClick(event: MouseEvent): Boolean {
                choose()
                return true
            }
        }.installOn(list)

        popup = JBPopupFactory.getInstance().createComponentPopupBuilder(JBScrollPane(list), list)
            .setProject(project)
            .setTitle("CodeSonar Analyses of ${result.project}")
            .setAdText(HINTS)
            .setRequestFocus(true)
            .setFocusable(true)
            .setMovable(true)
            .setResizable(true)
            .setCancelOnClickOutside(true)
            .setCancelKeyEnabled(true)
            .createPopup()
        popup.showCenteredInCurrentWindow(project)
        list.ensureIndexIsVisible(list.selectedIndex)
    }

    private fun selected(): Analysis? = list.selectedValue

    private fun finished(analysis: Analysis): Boolean {
        if (!analysis.finished) {
            Notifier.error(project, "Analysis ${analysis.id} is not finished yet")
        }

        return analysis.finished
    }

    /** Shows the analysis and closes the list. */
    private fun choose() {
        val analysis = selected() ?: return

        if (finished(analysis)) {
            popup.cancel()
            service.execute(Commands.LOAD, mapOf("analysis" to analysis.id)) {}
        }
    }

    /** Shows the analysis, or hides it if it is shown, and marks it in the list. */
    private fun toggle() {
        val analysis = selected() ?: return
        if (!finished(analysis)) return

        val failed = {
            analysis.loading = false
            list.repaint()
        }
        val shown = { result: JsonElement ->
            mark(analyses, Status.parse(result).hubId)
            list.repaint()
            // A shown analysis is cached afterwards.
            service.execute(Commands.ANALYSES) { updateCached(it) }
        }

        if (analysis.current) {
            service.execute(Commands.HIDE, failed = failed, done = shown)
            return
        }

        analysis.loading = true
        list.repaint()
        service.execute(Commands.LOAD, mapOf("analysis" to analysis.id), failed, shown)
    }

    /** Takes over which analyses are cached from the analyses of the server. */
    private fun updateCached(result: JsonElement) {
        val cached = Analyses.parse(result).analyses.filter { it.cached }.map { it.id }.toSet()

        analyses.forEach { it.cached = it.id in cached }
        list.repaint()
    }

    /** Deletes the local copy of an analysis, so that it is downloaded again. */
    private fun uncache() {
        val analysis = selected() ?: return

        if (!analysis.cached) {
            Notifier.error(project, "Analysis ${analysis.id} has no local copy")
            return
        }

        service.execute(Commands.UNCACHE, mapOf("analysis" to analysis.id)) {
            updateCached(it)
            Notifier.info(project, "Deleted the local copy of analysis ${analysis.id}")
        }
    }

    /** Deletes the analysis from the hub after a confirmation. */
    private fun delete() {
        val analysis = selected() ?: return
        val index = list.selectedIndex
        val question = "Delete analysis ${analysis.id} \"${analysis.name}\" of ${result.project} " +
            "from the hub? This cannot be undone."

        popup.cancel()

        if (Messages.showYesNoDialog(project, question, "CodeSonar", Messages.getWarningIcon()) != Messages.YES) {
            AnalysesPopup(project, result).show(index)
            return
        }

        service.execute(Commands.DELETE_ANALYSIS, mapOf("analysis" to analysis.id)) {
            val remaining = Analyses.parse(it)

            if (remaining.deleted) {
                Notifier.info(project, "Deleted analysis ${analysis.id} \"${analysis.name}\"")
            } else {
                Notifier.error(project, remaining.message ?: "The hub did not delete analysis ${analysis.id}")
            }

            AnalysesPopup(project, remaining).show(index)
        }
    }

    companion object {
        const val HINTS = "Enter show  ·  Space show/hide  ·  x delete locally  ·  d delete on hub  ·  q close"

        /** Marks the analysis with the hub id [shown] as the shown one. */
        fun mark(analyses: List<Analysis>, shown: String?) {
            analyses.forEach {
                it.current = shown != null && shown == it.id.toString()
                it.loading = false
            }
        }
    }
}

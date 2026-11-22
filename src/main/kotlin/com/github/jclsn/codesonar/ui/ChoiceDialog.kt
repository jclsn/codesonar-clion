package com.github.jclsn.codesonar.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.CheckBoxList
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SearchTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent

/** What can be ticked in a [ChoiceDialog]. */
class Choice<T>(val item: T, var ticked: Boolean)

/** The choices whose search text contains [filter], ignoring case. */
fun <T> filterChoices(choices: List<Choice<T>>, filter: String, searchText: (T) -> String): List<Choice<T>> {
    val wanted = filter.trim().lowercase()

    return if (wanted.isEmpty()) choices else choices.filter { wanted in searchText(it.item).lowercase() }
}

/**
 * A dialog to tick presets or checks, with a field that filters them while
 * typing. [label] is the text of an item, which may depend on its tick.
 */
class ChoiceDialog<T>(
    project: Project,
    dialogTitle: String,
    private val choices: List<Choice<T>>,
    private val searchText: (T) -> String,
    private val label: (Choice<T>) -> String,
) : DialogWrapper(project) {
    private val search = SearchTextField(false)
    private val list = CheckBoxList<Choice<T>>()

    init {
        title = dialogTitle
        setOKButtonText("Save")
        list.setCheckBoxListListener { index, ticked ->
            val choice = list.getItemAt(index) ?: return@setCheckBoxListListener

            choice.ticked = ticked
            list.updateItem(choice, choice, label(choice))
        }
        search.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(event: DocumentEvent) = showChoices()
        })
        showChoices()
        init()
    }

    private fun showChoices() {
        val shown = filterChoices(choices, search.text, searchText)

        list.clear()
        shown.forEach { list.addItem(it, label(it), it.ticked) }
        list.emptyText.text = "Nothing contains \"${search.text.trim()}\""
    }

    override fun createCenterPanel(): JComponent = JPanel(BorderLayout(0, JBUI.scale(6))).apply {
        add(search, BorderLayout.NORTH)
        add(ScrollPaneFactory.createScrollPane(list), BorderLayout.CENTER)
        preferredSize = Dimension(JBUI.scale(640), JBUI.scale(480))
    }

    override fun getPreferredFocusedComponent(): JComponent = search.textEditor

    override fun getDimensionServiceKey(): String = "CodeSonar.ChoiceDialog"
}

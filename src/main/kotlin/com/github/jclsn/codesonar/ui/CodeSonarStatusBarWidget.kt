package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.CodeSonarListener
import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.actions.StatusAction
import com.github.jclsn.codesonar.model.Status
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.Consumer
import java.awt.Component
import java.awt.event.MouseEvent

/**
 * Shows the analysis in the status bar: what is loading, else how many of
 * its warnings are shown. A click opens the status.
 */
class CodeSonarStatusBarWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.TextPresentation {
    private var statusBar: StatusBar? = null

    override fun ID(): String = ID

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
        project.messageBus.connect(this).subscribe(CodeSonarListener.TOPIC, object : CodeSonarListener {
            override fun loading(message: String) = update()

            override fun loaded(status: Status) = update()
        })
    }

    private fun update() {
        ApplicationManager.getApplication().invokeLater {
            statusBar?.updateWidget(ID)
        }
    }

    override fun dispose() {
        statusBar = null
    }

    override fun getText(): String {
        val service = CodeSonarService.getInstance(project)

        return text(service.status, service.loading != null)
    }

    override fun getTooltipText(): String {
        val service = CodeSonarService.getInstance(project)

        return service.loading ?: "CodeSonar: ${service.status.summary()}"
    }

    override fun getAlignment(): Float = Component.CENTER_ALIGNMENT

    override fun getClickConsumer(): Consumer<MouseEvent> = Consumer { StatusAction.show(project) }

    companion object {
        const val ID = "CodeSonar"

        /** The short text of the widget; the tooltip has the details. */
        fun text(status: Status, loading: Boolean): String = when {
            loading -> "CodeSonar: loading..."
            status.hidden -> "CodeSonar: hidden"
            status.loaded -> "CodeSonar: ${status.shown}/${status.total}"
            else -> "CodeSonar"
        }
    }
}

class CodeSonarStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = CodeSonarStatusBarWidget.ID

    override fun getDisplayName(): String = "CodeSonar"

    override fun createWidget(project: Project): StatusBarWidget = CodeSonarStatusBarWidget(project)
}

package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.CodeSonarService
import com.github.jclsn.codesonar.model.uriToPath
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.HTMLEditorKitBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.Color
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JEditorPane
import javax.swing.event.HyperlinkEvent

/** A popup with HTML that is closed and scrolled with keys, like in Vim. */
object HtmlPopup {
    /** The colors of the current theme. */
    fun palette(): Palette {
        fun html(color: Color) = ColorUtil.toHtmlColor(color)

        return Palette(
            accent = html(JBUI.CurrentTheme.Link.Foreground.ENABLED),
            changed = html(JBColor(Color(0xA36B00), Color(0xE0A84A))),
            muted = html(UIUtil.getContextHelpForeground()),
            unchanged = html(UIUtil.getLabelSuccessForeground()),
            moved = html(JBUI.CurrentTheme.Link.Foreground.ENABLED),
        )
    }

    /**
     * Shows [html] at the caret of [editor], else in the middle of the
     * window. [hints] tells the keys below the text; [keys] are those that
     * close the popup and do something. `q` and Escape close it, `j`, `k`,
     * `Ctrl+D`, `Ctrl+U`, `g` and `G` scroll.
     */
    fun show(
        project: Project,
        editor: Editor?,
        title: String,
        html: String,
        hints: String,
        keys: Map<Char, () -> Unit> = emptyMap(),
    ) {
        val pane = JEditorPane().apply {
            editorKit = HTMLEditorKitBuilder().withWordWrapViewFactory().build()
            isEditable = false
            border = JBUI.Borders.empty(6, 10)
            text = html
            caretPosition = 0
        }
        val scroll = JBScrollPane(pane).apply { border = JBUI.Borders.empty() }
        val width = JBUI.scale(640)

        pane.setSize(width, Short.MAX_VALUE.toInt())
        scroll.preferredSize = Dimension(
            width + scroll.verticalScrollBar.preferredSize.width,
            (pane.preferredSize.height + JBUI.scale(4)).coerceAtMost(JBUI.scale(520)),
        )

        val popup = JBPopupFactory.getInstance().createComponentPopupBuilder(scroll, pane)
            .setProject(project)
            .setTitle(title)
            .setAdText(hints)
            .setRequestFocus(true)
            .setFocusable(true)
            .setMovable(true)
            .setResizable(true)
            .setCancelOnClickOutside(true)
            .setCancelKeyEnabled(true)
            .createPopup()

        pane.addHyperlinkListener { event ->
            if (event.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                follow(project, popup, event.description ?: event.url?.toString().orEmpty())
            }
        }
        pane.addKeyListener(Keys(popup, scroll, keys))

        if (editor != null) {
            popup.showInBestPositionFor(editor)
        } else {
            popup.showCenteredInCurrentWindow(project)
        }
    }

    /** Opens a place in the code in the editor, other links in the browser. */
    private fun follow(project: Project, popup: JBPopup, href: String) {
        val location = Html.parseLocation(href)

        if (location == null) {
            Browser.open(project, href)
            return
        }

        val path = uriToPath(location.second) ?: return
        popup.cancel()
        CodeSonarService.getInstance(project).openFile(path, (location.first - 1).coerceAtLeast(0))
    }

    private class Keys(
        private val popup: JBPopup,
        private val scroll: JBScrollPane,
        private val keys: Map<Char, () -> Unit>,
    ) : KeyAdapter() {
        override fun keyPressed(event: KeyEvent) {
            val bar = scroll.verticalScrollBar
            val line = bar.unitIncrement.coerceAtLeast(JBUI.scale(16))
            val page = scroll.viewport.height / 2
            val char = event.keyChar

            when {
                event.isControlDown && event.keyCode == KeyEvent.VK_D -> bar.value += page
                event.isControlDown && event.keyCode == KeyEvent.VK_U -> bar.value -= page
                event.isControlDown || event.isAltDown || event.isMetaDown -> return
                char == 'q' -> popup.cancel()
                char == 'j' -> bar.value += line
                char == 'k' -> bar.value -= line
                char == 'g' -> bar.value = bar.minimum
                char == 'G' -> bar.value = bar.maximum
                char in keys -> {
                    popup.cancel()
                    keys.getValue(char)()
                }
                else -> return
            }

            event.consume()
        }
    }
}

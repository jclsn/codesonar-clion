package com.github.jclsn.codesonar.ui

import com.intellij.ui.ColoredListCellRenderer
import javax.swing.JList

/** Renders the items of a list as [text], in the font of the list. */
internal fun <T> textRenderer(text: (T) -> String): ColoredListCellRenderer<T> =
    object : ColoredListCellRenderer<T>() {
        override fun customizeCellRenderer(
            list: JList<out T>,
            value: T,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean,
        ) {
            font = list.font
            append(text(value))
        }
    }

package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.Notifier
import com.github.jclsn.codesonar.model.isWebLink
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project

/** Opens links of analyses and the hub, which are not trusted. */
object Browser {
    /** Opens a web link in the browser, and refuses any other link. */
    fun open(project: Project, url: String) {
        if (isWebLink(url)) {
            BrowserUtil.browse(url.trim(), project)
        } else {
            Notifier.error(project, "Not a web link: $url")
        }
    }
}

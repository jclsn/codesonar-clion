package com.github.jclsn.codesonar

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

/** Messages of the plugin, as notifications. */
object Notifier {
    const val GROUP = "CodeSonar"

    fun info(project: Project, text: String) = notify(project, text, NotificationType.INFORMATION)

    /** Shows an error. It starts with a capital letter, also if the server wrote it. */
    fun error(project: Project, text: String) =
        notify(project, capitalize(text), NotificationType.ERROR)

    fun capitalize(text: String): String = text.replaceFirstChar { it.uppercase() }

    private fun notify(project: Project, text: String, type: NotificationType) {
        NotificationGroupManager.getInstance().getNotificationGroup(GROUP)
            .createNotification("CodeSonar", text, type)
            .notify(project)
    }
}

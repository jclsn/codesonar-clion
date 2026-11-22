package com.github.jclsn.codesonar

import com.github.jclsn.codesonar.model.Status
import com.intellij.util.messages.Topic

/**
 * What the server tells about the shown analysis. The listeners are called
 * in the thread that received the notification, not in the UI thread.
 */
interface CodeSonarListener {
    /** An analysis starts loading; [message] says which one. */
    fun loading(message: String) {}

    /** An analysis was shown or hidden, or its warnings changed. */
    fun loaded(status: Status) {}

    companion object {
        @Topic.ProjectLevel
        val TOPIC: Topic<CodeSonarListener> = Topic.create("CodeSonar", CodeSonarListener::class.java)
    }
}

package com.github.jclsn.codesonar.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ConnectDialogTest {
    @Test
    fun `connects with what was entered`() {
        assertEquals(emptyMap(), ConnectDialog.argument("", " ", "ignored"))
        assertEquals(mapOf("url" to "http://hub:7340"), ConnectDialog.argument(" http://hub:7340 ", "", ""))
        assertEquals(
            mapOf("url" to "http://hub:7340", "username" to "alice", "password" to "secret"),
            ConnectDialog.argument("http://hub:7340", "alice", "secret"),
        )
    }
}

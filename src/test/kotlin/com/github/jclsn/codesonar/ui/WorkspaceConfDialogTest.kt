package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.model.WorkspaceProject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WorkspaceConfDialogTest {
    private val suggested = WorkspaceProject.Suggested("ws", "make -B", true, "make", "")

    @Test
    fun `says what the workspace configuration lacks`() {
        val missing = WorkspaceProject("/ws/codesonar.conf", false, false, suggested)

        assertEquals("No codesonar.conf found.", WorkspaceConfDialog.missing(missing))
        assertEquals(
            "No configuration block for codesonar-lsp in codesonar.conf.",
            WorkspaceConfDialog.missing(missing.copy(workspaceConfExists = true)),
        )
    }

    @Test
    fun `needs a project name and the build commands`() {
        assertNull(WorkspaceConfDialog.invalid("ws", "make -B", true, "make"))
        assertNull(WorkspaceConfDialog.invalid("ws", "make -B", false, ""))
        assertEquals(WorkspaceConfDialog.Field.NAME, WorkspaceConfDialog.invalid(" ", "make -B", true, "make"))
        assertEquals(WorkspaceConfDialog.Field.BUILD, WorkspaceConfDialog.invalid("ws", "", true, "make"))
        assertEquals(WorkspaceConfDialog.Field.INCREMENTAL_BUILD, WorkspaceConfDialog.invalid("ws", "make -B", true, " "))
    }

    @Test
    fun `starts the configuration with the fields`() {
        assertEquals(
            mapOf(
                "project" to "ws", "build" to "make -B", "incremental" to true,
                "incrementalBuild" to "make", "hubAddress" to "hub:7340",
            ),
            WorkspaceConfDialog.argument(" hub:7340", true, "ws ", "make -B", "make"),
        )
    }

    @Test
    fun `has no incremental build command without incremental builds`() {
        val argument = WorkspaceConfDialog.argument("", false, "ws", "make -B", "make")

        assertEquals(false, argument["incremental"])
        assertEquals("", argument["incrementalBuild"])
    }
}

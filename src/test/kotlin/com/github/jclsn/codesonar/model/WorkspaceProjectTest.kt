package com.github.jclsn.codesonar.model

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkspaceProjectTest {
    private fun json(text: String) = JsonParser.parseString(text)

    @Test
    fun `parses the workspace configuration`() {
        val workspace = WorkspaceProject.parse(
            json(
                """{"prefix": "/ws/.codesonar/project", "conf": "/ws/.codesonar/project.conf", "confExists": false,
                    "created": false, "incremental": true, "workspaceConf": "/ws/codesonar.conf",
                    "workspaceConfExists": true, "section": false,
                    "suggested": {"project": "ws", "build": "make -B", "incremental": false,
                                  "incrementalBuild": "make", "hubAddress": null}}""",
            ),
        )!!

        assertEquals("/ws/codesonar.conf", workspace.workspaceConf)
        assertTrue(workspace.workspaceConfExists)
        assertFalse(workspace.section)
        assertEquals(WorkspaceProject.Suggested("ws", "make -B", false, "make", ""), workspace.suggested)
    }

    @Test
    fun `suggests incremental analyses by default`() {
        val workspace = WorkspaceProject.parse(json("""{"workspaceConf": "/ws/codesonar.conf", "section": true}"""))!!

        assertTrue(workspace.section)
        assertTrue(workspace.suggested.incremental)
        assertNull(WorkspaceProject.parse(json("{}")))
    }
}

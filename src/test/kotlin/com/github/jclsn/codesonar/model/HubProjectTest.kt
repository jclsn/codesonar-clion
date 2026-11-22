package com.github.jclsn.codesonar.model

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals

class HubProjectTest {
    @Test
    fun `parses the projects of the hub`() {
        val projects = HubProject.parseList(
            JsonParser.parseString("""[{"id": 3, "name": "mixxx"}, {"id": "9", "name": "test_project"}]"""),
        )

        assertEquals(listOf(HubProject("3", "mixxx"), HubProject("9", "test_project")), projects)
        assertEquals(emptyList(), HubProject.parseList(null))
    }
}

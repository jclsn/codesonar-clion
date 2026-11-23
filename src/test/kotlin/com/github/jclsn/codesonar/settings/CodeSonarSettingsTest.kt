package com.github.jclsn.codesonar.settings

import com.intellij.util.xmlb.XmlSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CodeSonarSettingsTest {
    @Test
    fun `the state survives the settings file`() {
        val state = CodeSonarSettings.State(hubUrl = "https://hub:7340", username = "me", markChanged = true)

        val element = XmlSerializer.serialize(state)

        assertEquals(state, XmlSerializer.deserialize(element, CodeSonarSettings.State::class.java))
    }
}

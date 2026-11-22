package com.github.jclsn.codesonar.ui

import com.github.jclsn.codesonar.actions.ChecksAction
import com.github.jclsn.codesonar.actions.PresetsAction
import com.github.jclsn.codesonar.model.Check
import com.github.jclsn.codesonar.model.Preset
import kotlin.test.Test
import kotlin.test.assertEquals

class ChoiceTest {
    private val presets = listOf(
        Preset("misrac2025", "MISRA C:2025", true),
        Preset("security", "Security checks", false),
        Preset("cert", null, false),
    ).map { Choice(it, it.selected) }

    private fun names(filter: String) =
        filterChoices(presets, filter) { "${it.name} ${it.description.orEmpty()}" }.map { it.item.name }

    @Test
    fun `filters by name and description`() {
        assertEquals(listOf("misrac2025", "security", "cert"), names(""))
        assertEquals(listOf("misrac2025"), names("misra c"))
        assertEquals(listOf("security"), names(" SEC "))
        assertEquals(listOf("misrac2025", "security"), names("s"))
        assertEquals(emptyList(), names("autosar"))
    }

    @Test
    fun `labels presets with their descriptions`() {
        assertEquals("misrac2025  –  MISRA C:2025", PresetsAction.label(presets[0].item))
        assertEquals("cert", PresetsAction.label(presets[2].item))
    }

    @Test
    fun `tells which presets were saved`() {
        assertEquals(
            "Analyses use the presets misrac2025, cert (codesonar.conf)",
            PresetsAction.saved(listOf("misrac2025", "cert"), "/ws/codesonar.conf"),
        )
        assertEquals("Analyses use no presets (codesonar.conf)", PresetsAction.saved(emptyList(), "/ws/codesonar.conf"))
    }

    @Test
    fun `marks checks that differ from the default`() {
        val check = Check("Null Pointer Dereference", "C/C++", default = true, enabled = true)

        assertEquals("Null Pointer Dereference", ChecksAction.label(check, ticked = true))
        assertEquals("Null Pointer Dereference  (changed)", ChecksAction.label(check, ticked = false))
    }

    @Test
    fun `saves the checks whose tick changed`() {
        val choices = listOf(
            Choice(Check("Null Pointer Dereference", "C/C++", default = true, enabled = true), ticked = false),
            Choice(Check("Function Too Long", "C/C++", default = false, enabled = false), ticked = true),
            Choice(Check("Unused Value", "C/C++", default = true, enabled = true), ticked = true),
        )

        assertEquals(
            listOf(
                mapOf("class" to "Null Pointer Dereference", "enabled" to false),
                mapOf("class" to "Function Too Long", "enabled" to true),
            ),
            ChecksAction.changed(choices),
        )
    }
}

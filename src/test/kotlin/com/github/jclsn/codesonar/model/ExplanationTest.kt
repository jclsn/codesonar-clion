package com.github.jclsn.codesonar.model

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExplanationTest {
    companion object {
        const val JSON = """[{"title": "Null Pointer Dereference", "ruleId": "LANG.MEM.NPD",
            "message": "`p` is dereferenced by printf() but may be **NULL**.",
            "description": "See [the manual](http://hub:7340/install/doc/NPD.html).",
            "status": "changed",
            "related": [{"uri": "file:///ws/src/utils.c", "line": 7, "message": "p is assigned here"}],
            "events": [
                {"number": 1, "uri": "file:///ws/src/main.c", "line": 12, "message": "malloc() returns NULL", "depth": 0},
                {"number": null, "uri": "file:///ws/src/utils.c", "line": 7, "message": "p <= 0", "depth": 1}],
            "rules": [
                {"id": "CWE:476", "title": "NULL Pointer Dereference", "relationship": "intersects",
                 "url": "https://cwe.mitre.org/data/definitions/476.html"},
                {"id": "CERT-C:EXP34-C", "title": null, "relationship": "follows", "url": null}],
            "helpUri": "http://hub:7340/install/doc/NPD.html",
            "hubUri": "http://hub:7340/warninginstance/42.html",
            "markdown": "..."},
            {"title": "Warning", "ruleId": null, "message": "Something", "description": null,
             "status": "unchanged", "related": [], "events": [], "rules": [{"id": "MISRA:1.3", "title": null,
             "relationship": null, "url": null}], "helpUri": null, "hubUri": null}]"""

        fun explanations(): List<Explanation> = Explanation.parseList(JsonParser.parseString(JSON))
    }

    @Test
    fun `parses an explanation with details`() {
        val explanation = explanations()[0]

        assertEquals("Null Pointer Dereference", explanation.title)
        assertEquals("LANG.MEM.NPD", explanation.ruleId)
        assertEquals("changed", explanation.status)
        assertEquals(Explanation.Location("file:///ws/src/utils.c", 7, "p is assigned here"), explanation.related.single())
        assertEquals(listOf(1, null), explanation.events.map { it.number })
        assertEquals(listOf(0, 1), explanation.events.map { it.depth })
        assertEquals(12, explanation.events[0].line)
        assertEquals("http://hub:7340/warninginstance/42.html", explanation.hubUri)
    }

    @Test
    fun `tells violations from symptoms`() {
        val (violation, symptom) = explanations()[0].rules

        assertFalse(violation.isSymptom)
        assertEquals("NULL Pointer Dereference", violation.title)
        assertTrue(symptom.isSymptom)
        assertNull(symptom.title)
    }

    @Test
    fun `parses an explanation without details`() {
        val explanation = explanations()[1]

        assertNull(explanation.ruleId)
        assertNull(explanation.description)
        assertNull(explanation.helpUri)
        assertEquals(emptyList(), explanation.events)
        assertEquals(emptyList(), Explanation.parseList(JsonParser.parseString("[]")))
    }
}

package com.github.jclsn.codesonar.model

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals

class AnalysisOptionsTest {
    private fun json(text: String) = JsonParser.parseString(text)

    @Test
    fun `parses the presets and checks`() {
        val options = AnalysisOptions.parse(
            json(
                """{"conf": "/ws/codesonar.conf",
                    "presets": [{"name": "misrac2025", "description": "MISRA C:2025", "selected": true},
                                {"name": "security", "description": null, "selected": false}],
                    "checks": [{"class": "Null Pointer Dereference", "language": "C/C++", "default": true, "enabled": false},
                               {"class": "Function Too Long", "language": "C/C++", "default": false, "enabled": false}]}""",
            ),
        )

        assertEquals("/ws/codesonar.conf", options.conf)
        assertEquals(
            listOf(Preset("misrac2025", "MISRA C:2025", true), Preset("security", null, false)),
            options.presets,
        )
        assertEquals(
            listOf(
                Check("Null Pointer Dereference", "C/C++", default = true, enabled = false),
                Check("Function Too Long", "C/C++", default = false, enabled = false),
            ),
            options.checks,
        )
        assertEquals(options.presets, Preset.parseList(json("""[{"name": "misrac2025", "description": "MISRA C:2025",
            "selected": true}, {"name": "security", "description": null, "selected": false}]""")))
    }
}

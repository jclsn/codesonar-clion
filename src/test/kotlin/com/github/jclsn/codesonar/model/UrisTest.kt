package com.github.jclsn.codesonar.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UrisTest {
    @Test
    fun `opens only web links`() {
        assertTrue(isWebLink("https://hub:7340/warninginstance/42.html"))
        assertTrue(isWebLink("http://hub:7340/"))
        assertTrue(isWebLink(" HTTPS://hub/ "))

        assertFalse(isWebLink("file:///etc/passwd"))
        assertFalse(isWebLink("javascript:alert(1)"))
        assertFalse(isWebLink("jetbrains://clion/navigate"))
        assertFalse(isWebLink("/usr/bin/xterm"))
        assertFalse(isWebLink("https:///no-host"))
        assertFalse(isWebLink("not a link"))
        assertFalse(isWebLink(""))
    }
}

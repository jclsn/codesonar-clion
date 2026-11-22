package com.github.jclsn.codesonar.lsp

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SourceFilesTest {
    @Test
    fun `takes c and cpp sources and headers`() {
        for (name in listOf("main.c", "utils.h", "widget.cpp", "widget.hpp", "a.cc", "a.hh", "A.CXX", "impl.inl")) {
            assertTrue(SourceFiles.isSource(name), name)
        }
    }

    @Test
    fun `skips other files`() {
        for (name in listOf("Makefile", "codesonar.conf", "main.rs", "notes.txt", "c")) {
            assertFalse(SourceFiles.isSource(name), name)
        }
    }
}

package com.droidxampp.termux

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TermuxCommandTest {
    @Test
    fun scriptPath_pointsInsideScriptDir() {
        Service.values().forEach {
            val path = TermuxCommand.scriptPath(it)
            assertTrue(path.startsWith(TermuxCommand.SCRIPT_DIR + "/"))
            assertTrue(path.endsWith(".sh"))
        }
    }

    @Test
    fun actions_haveExpectedArguments() {
        assertEquals(listOf("start", "stop", "restart", "status", "version"), Action.values().map { it.arg })
    }
}

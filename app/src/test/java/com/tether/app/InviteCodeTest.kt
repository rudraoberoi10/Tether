package com.tether.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InviteCodeTest {

    private val alphabet = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"

    @Test
    fun inviteCode_containsExactSixCharacters() {
        val code = generateSampleCode()
        assertEquals(6, code.length)
    }

    @Test
    fun inviteCode_containsOnlyAllowedAlphabetCharacters() {
        val code = generateSampleCode()
        code.forEach { char ->
            assertTrue("Character $char must be in the approved alphabet", alphabet.contains(char))
        }
    }

    @Test
    fun inviteCode_neverContainsAmbiguousCharacters() {
        val ambiguousChars = listOf('0', 'O', '1', 'I', 'L')
        val code = generateSampleCode()
        ambiguousChars.forEach { ambiguous ->
            assertTrue("Code must not contain ambiguous character $ambiguous", !code.contains(ambiguous))
        }
    }

    private fun generateSampleCode(): String {
        return (1..6)
            .map { alphabet.random() }
            .joinToString("")
    }
}